package com.sapphyx.logeon.test.repository.user

import app.cash.turbine.test
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Transaction
import com.sapphyx.logeon.data.model.UserProfile
import com.sapphyx.logeon.data.repository.OnboardingResponse
import com.sapphyx.logeon.data.repository.UpdateProfileResponse
import com.sapphyx.logeon.data.repository.UserProfileState
import com.sapphyx.logeon.data.repository.UserRepository
import com.sapphyx.logeon.data.repository.util.user.UsernameValidationResult
import com.sapphyx.logeon.data.repository.util.user.UsernameValidator
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UserRepositoryTest {

    private val firestore: FirebaseFirestore = mockk(relaxed = true)
    private val usernameValidator: UsernameValidator = mockk()
    private lateinit var repository: UserRepository

    @Before
    fun setUp() {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
        repository = UserRepository(firestore, usernameValidator)
    }

    @After
    fun tearDown() {
        unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    // --- observeUserProfile ---

    @Test
    fun `observeUserProfile emits Loading then Success when profile is completed`() = runTest {
        val usersCollection: CollectionReference = mockk()
        val userDoc: DocumentReference = mockk()
        val registration: ListenerRegistration = mockk(relaxed = true)
        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        val snapshot: DocumentSnapshot = mockk()

        val expectedProfile = UserProfile(
            uid = "uid-1",
            email = "user@logeon.com",
            username = "alex",
            dateOfBirthMillis = 946684800000L,
            country = "Romania",
            onboardingCompleted = true
        )

        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document("uid-1") } returns userDoc
        every { userDoc.addSnapshotListener(capture(listenerSlot)) } answers {
            listenerSlot.captured.onEvent(snapshot, null)
            registration
        }
        every { snapshot.toObject(UserProfile::class.java) } returns expectedProfile

        repository.observeUserProfile("uid-1").test {
            assertEquals(UserProfileState.Loading, awaitItem())
            assertEquals(UserProfileState.Success(expectedProfile), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) { registration.remove() }
    }

    @Test
    fun `observeUserProfile emits Loading then NotFound when profile is missing or incomplete`() =
        runTest {
            val usersCollection: CollectionReference = mockk()
            val userDoc: DocumentReference = mockk()
            val registration: ListenerRegistration = mockk(relaxed = true)
            val listenerSlot = slot<EventListener<DocumentSnapshot>>()
            val snapshot: DocumentSnapshot = mockk()

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document("uid-1") } returns userDoc
            every { userDoc.addSnapshotListener(capture(listenerSlot)) } answers {
                listenerSlot.captured.onEvent(snapshot, null)
                registration
            }
            every { snapshot.toObject(UserProfile::class.java) } returns UserProfile(
                uid = "uid-1",
                onboardingCompleted = false
            )

            repository.observeUserProfile("uid-1").test {
                assertEquals(UserProfileState.Loading, awaitItem())
                assertEquals(UserProfileState.NotFound, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `observeUserProfile emits Loading then Error when snapshot listener fails`() = runTest {
        val usersCollection: CollectionReference = mockk()
        val userDoc: DocumentReference = mockk()
        val registration: ListenerRegistration = mockk(relaxed = true)
        val listenerSlot = slot<EventListener<DocumentSnapshot>>()
        val firestoreException: FirebaseFirestoreException = mockk {
            every { message } returns "Permission denied"
        }

        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document("uid-1") } returns userDoc
        every { userDoc.addSnapshotListener(capture(listenerSlot)) } answers {
            listenerSlot.captured.onEvent(null, firestoreException)
            registration
        }

        repository.observeUserProfile("uid-1").test {
            assertEquals(UserProfileState.Loading, awaitItem())
            assertEquals(UserProfileState.Error("Permission denied"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- checkUsernameAvailable ---

    @Test
    fun `checkUsernameAvailable returns validation error immediately when format is invalid`() =
        runTest {
            every { usernameValidator("ab") } returns UsernameValidationResult.TOO_SHORT

            val result = repository.checkUsernameAvailable("  ab ")

            assertEquals(UsernameValidationResult.TOO_SHORT, result)
            verify(exactly = 0) { firestore.collection("usernames") }
        }

    @Test
    fun `checkUsernameAvailable returns ALREADY_TAKEN when document exists and CORRECT when absent`() =
        runTest {
            val usernamesCollection: CollectionReference = mockk()
            val usernameDoc: DocumentReference = mockk()
            val getTask: Task<DocumentSnapshot> = mockk()
            val snapshot: DocumentSnapshot = mockk()

            every { usernameValidator("alex") } returns UsernameValidationResult.CORRECT
            every { firestore.collection("usernames") } returns usernamesCollection
            every { usernamesCollection.document("alex") } returns usernameDoc
            every { usernameDoc.get() } returns getTask
            coEvery { getTask.await() } returns snapshot

            every { snapshot.exists() } returns true
            assertEquals(
                UsernameValidationResult.ALREADY_TAKEN,
                repository.checkUsernameAvailable("alex")
            )

            every { snapshot.exists() } returns false
            assertEquals(
                UsernameValidationResult.CORRECT,
                repository.checkUsernameAvailable("alex")
            )
        }

    // --- completeOnboarding ---

    @Test
    fun `completeOnboarding emits validation errors before touching Firestore`() = runTest {
        every { usernameValidator("") } returns UsernameValidationResult.EMPTY
        repository.completeOnboarding("uid-1", "a@b.com", "", null, null, 1000L, "Romania")
            .test {
                assertEquals(
                    OnboardingResponse.UsernameError(UsernameValidationResult.EMPTY),
                    awaitItem()
                )
                awaitComplete()
            }

        every { usernameValidator("alex") } returns UsernameValidationResult.CORRECT
        repository.completeOnboarding("uid-1", "a@b.com", "alex", null, null, null, "Romania")
            .test {
                assertEquals(OnboardingResponse.DateOfBirthRequired, awaitItem())
                awaitComplete()
            }

        repository.completeOnboarding("uid-1", "a@b.com", "alex", null, null, 1000L, "   ")
            .test {
                assertEquals(OnboardingResponse.CountryRequired, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun `completeOnboarding executes transaction and emits Loading then Success`() = runTest {
        val usernamesCollection: CollectionReference = mockk()
        val usersCollection: CollectionReference = mockk()
        val usernameDocRef: DocumentReference = mockk()
        val userDocRef: DocumentReference = mockk()
        val transaction: Transaction = mockk(relaxed = true)
        val existingSnapshot: DocumentSnapshot = mockk {
            every { exists() } returns false
        }
        val txFunctionSlot = slot<Transaction.Function<Unit>>()
        val txTask: Task<Unit> = mockk()

        every { usernameValidator("alex") } returns UsernameValidationResult.CORRECT
        every { firestore.collection("usernames") } returns usernamesCollection
        every { usernamesCollection.document("alex") } returns usernameDocRef
        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document("uid-1") } returns userDocRef
        every { transaction.get(usernameDocRef) } returns existingSnapshot

        every { firestore.runTransaction(capture(txFunctionSlot)) } answers {
            txFunctionSlot.captured.apply(transaction)
            txTask
        }
        coEvery { txTask.await() } returns Unit

        repository.completeOnboarding(
            uid = "uid-1",
            email = "user@logeon.com",
            username = "alex",
            displayName = "  Alex  ",
            bio = "   ",
            dateOfBirthMillis = 946684800000L,
            country = "Romania"
        ).test {
            assertEquals(OnboardingResponse.Loading, awaitItem())
            assertEquals(OnboardingResponse.Success, awaitItem())
            awaitComplete()
        }

        verify(exactly = 1) { transaction.set(usernameDocRef, mapOf("uid" to "uid-1")) }
        verify(exactly = 1) {
            transaction.set(
                userDocRef,
                UserProfile(
                    uid = "uid-1",
                    email = "user@logeon.com",
                    username = "alex",
                    displayName = "Alex",
                    bio = null,
                    dateOfBirthMillis = 946684800000L,
                    country = "Romania",
                    onboardingCompleted = true
                )
            )
        }
    }

    @Test
    fun `completeOnboarding emits UsernameError ALREADY_TAKEN when transaction finds existing username`() =
    runTest {
        val usernamesCollection: CollectionReference = mockk()
        val usersCollection: CollectionReference = mockk()
        val usernameDocRef: DocumentReference = mockk()
        val userDocRef: DocumentReference = mockk()
        val transaction: Transaction = mockk(relaxed = true)
        val existingSnapshot: DocumentSnapshot = mockk {
            every { exists() } returns true
        }
        val txFunctionSlot = slot<Transaction.Function<Any?>>()
        val txTask: Task<Any?> = mockk()
        every { usernameValidator("alex") } returns UsernameValidationResult.CORRECT
        every { firestore.collection("usernames") } returns usernamesCollection
        every { usernamesCollection.document("alex") } returns usernameDocRef
        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document("uid-1") } returns userDocRef
        every { transaction.get(usernameDocRef) } returns existingSnapshot
        every { firestore.runTransaction(capture(txFunctionSlot)) } answers {
            txFunctionSlot.captured.apply(transaction)
            txTask
        }
        coEvery { txTask.await() } returns Unit
        repository.completeOnboarding(
            uid = "uid-1",
            email = "user@logeon.com",
            username = "alex",
            displayName = null,
            bio = null,
            dateOfBirthMillis = 946684800000L,
            country = "Romania"
        ).test {
            assertEquals(OnboardingResponse.Loading, awaitItem())
            assertEquals(
                OnboardingResponse.UsernameError(UsernameValidationResult.ALREADY_TAKEN),
                awaitItem()
            )
            awaitComplete()
        }
    }

    // --- updateUserProfile ---

    @Test
    fun `updateUserProfile emits validation errors before touching Firestore`() = runTest {
        every { usernameValidator("ab") } returns UsernameValidationResult.TOO_SHORT
        repository.updateUserProfile("uid-1", "alex", "ab", null, null, "Romania")
            .test {
                assertEquals(
                    UpdateProfileResponse.UsernameError(UsernameValidationResult.TOO_SHORT),
                    awaitItem()
                )
                awaitComplete()
            }

        every { usernameValidator("alex") } returns UsernameValidationResult.CORRECT
        repository.updateUserProfile("uid-1", "alex", "alex", null, null, "   ")
            .test {
                assertEquals(UpdateProfileResponse.CountryRequired, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun `updateUserProfile updates user document without touching usernames when username is unchanged`() =
        runTest {
            val usernamesCollection: CollectionReference = mockk()
            val usersCollection: CollectionReference = mockk()
            val newUsernameDocRef: DocumentReference = mockk()
            val userDocRef: DocumentReference = mockk()
            val transaction: Transaction = mockk(relaxed = true)
            val txFunctionSlot = slot<Transaction.Function<Any?>>()
            val txTask: Task<Any?> = mockk()

            every { usernameValidator("alex") } returns UsernameValidationResult.CORRECT
            every { firestore.collection("usernames") } returns usernamesCollection
            every { usernamesCollection.document("alex") } returns newUsernameDocRef
            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document("uid-1") } returns userDocRef

            every { firestore.runTransaction(capture(txFunctionSlot)) } answers {
                txFunctionSlot.captured.apply(transaction)
                txTask
            }
            coEvery { txTask.await() } returns Unit

            repository.updateUserProfile(
                uid = "uid-1",
                oldUsername = "alex",
                newUsername = "alex",
                displayName = "  Alex Updated ",
                bio = "  ",
                country = "Romania"
            ).test {
                assertEquals(UpdateProfileResponse.Loading, awaitItem())
                assertEquals(UpdateProfileResponse.Success, awaitItem())
                awaitComplete()
            }

            verify(exactly = 0) { transaction.get(any()) }
            verify(exactly = 0) { transaction.set(any(), any()) }
            verify(exactly = 0) { transaction.delete(any()) }
            verify(exactly = 1) {
                transaction.update(
                    userDocRef,
                    mapOf(
                        "username" to "alex",
                        "displayName" to "Alex Updated",
                        "bio" to null,
                        "country" to "Romania"
                    )
                )
            }
        }

    @Test
    fun `updateUserProfile swaps username reservations atomically when username changes`() =
        runTest {
            val usernamesCollection: CollectionReference = mockk()
            val usersCollection: CollectionReference = mockk()
            val newUsernameDocRef: DocumentReference = mockk()
            val oldUsernameDocRef: DocumentReference = mockk()
            val userDocRef: DocumentReference = mockk()
            val transaction: Transaction = mockk(relaxed = true)
            val newUsernameSnapshot: DocumentSnapshot = mockk {
                every { exists() } returns false
            }
            val txFunctionSlot = slot<Transaction.Function<Any?>>()
            val txTask: Task<Any?> = mockk()

            every { usernameValidator("alex_new") } returns UsernameValidationResult.CORRECT
            every { firestore.collection("usernames") } returns usernamesCollection
            every { usernamesCollection.document("alex_new") } returns newUsernameDocRef
            every { usernamesCollection.document("alex_old") } returns oldUsernameDocRef
            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document("uid-1") } returns userDocRef
            every { transaction.get(newUsernameDocRef) } returns newUsernameSnapshot

            every { firestore.runTransaction(capture(txFunctionSlot)) } answers {
                txFunctionSlot.captured.apply(transaction)
                txTask
            }
            coEvery { txTask.await() } returns Unit

            repository.updateUserProfile(
                uid = "uid-1",
                oldUsername = "alex_old",
                newUsername = "alex_new",
                displayName = "Alex",
                bio = "Hello",
                country = "Germany"
            ).test {
                assertEquals(UpdateProfileResponse.Loading, awaitItem())
                assertEquals(UpdateProfileResponse.Success, awaitItem())
                awaitComplete()
            }

            verify(exactly = 1) { transaction.set(newUsernameDocRef, mapOf("uid" to "uid-1")) }
            verify(exactly = 1) { transaction.delete(oldUsernameDocRef) }
            verify(exactly = 1) {
                transaction.update(
                    userDocRef,
                    mapOf(
                        "username" to "alex_new",
                        "displayName" to "Alex",
                        "bio" to "Hello",
                        "country" to "Germany"
                    )
                )
            }
        }

    @Test
    fun `updateUserProfile emits UsernameError ALREADY_TAKEN when new username exists`() =
        runTest {
            val usernamesCollection: CollectionReference = mockk()
            val usersCollection: CollectionReference = mockk()
            val newUsernameDocRef: DocumentReference = mockk()
            val oldUsernameDocRef: DocumentReference = mockk()
            val userDocRef: DocumentReference = mockk()
            val transaction: Transaction = mockk(relaxed = true)
            val newUsernameSnapshot: DocumentSnapshot = mockk {
                every { exists() } returns true
            }
            val txFunctionSlot = slot<Transaction.Function<Any?>>()
            val txTask: Task<Any?> = mockk()

            every { usernameValidator("taken_name") } returns UsernameValidationResult.CORRECT
            every { firestore.collection("usernames") } returns usernamesCollection
            every { usernamesCollection.document("taken_name") } returns newUsernameDocRef
            every { usernamesCollection.document("alex_old") } returns oldUsernameDocRef
            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document("uid-1") } returns userDocRef
            every { transaction.get(newUsernameDocRef) } returns newUsernameSnapshot

            every { firestore.runTransaction(capture(txFunctionSlot)) } answers {
                txFunctionSlot.captured.apply(transaction)
                txTask
            }
            coEvery { txTask.await() } returns Unit

            repository.updateUserProfile(
                uid = "uid-1",
                oldUsername = "alex_old",
                newUsername = "taken_name",
                displayName = null,
                bio = null,
                country = "Romania"
            ).test {
                assertEquals(UpdateProfileResponse.Loading, awaitItem())
                assertEquals(
                    UpdateProfileResponse.UsernameError(UsernameValidationResult.ALREADY_TAKEN),
                    awaitItem()
                )
                awaitComplete()
            }

            verify(exactly = 0) { transaction.delete(oldUsernameDocRef) }
        }

    // --- deleteUserData ---

    @Test
    fun `deleteUserData deletes both username reservation and user profile in transaction`() =
        runTest {
            val usernamesCollection: CollectionReference = mockk()
            val usersCollection: CollectionReference = mockk()
            val usernameDocRef: DocumentReference = mockk()
            val userDocRef: DocumentReference = mockk()
            val transaction: Transaction = mockk(relaxed = true)
            val txFunctionSlot = slot<Transaction.Function<Transaction>>()
            val txTask: Task<Transaction> = mockk()

            every { firestore.collection("usernames") } returns usernamesCollection
            every { usernamesCollection.document("alex") } returns usernameDocRef
            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document("uid-1") } returns userDocRef

            every { firestore.runTransaction(capture(txFunctionSlot)) } answers {
                txFunctionSlot.captured.apply(transaction)
                txTask
            }
            coEvery { txTask.await() } returns transaction

            repository.deleteUserData(uid = "uid-1", username = "Alex")

            verify(exactly = 1) { transaction.delete(usernameDocRef) }
            verify(exactly = 1) { transaction.delete(userDocRef) }
        }
}
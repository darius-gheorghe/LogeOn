package com.sapphyx.logeon.ui.composable.screens.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sapphyx.logeon.R
import com.sapphyx.logeon.ui.composable.baseScreen.BaseScreen
import com.sapphyx.logeon.ui.util.toErrorMessage
import com.sapphyx.logeon.viewModelImpl.onboarding.OnboardingScreenAction
import com.sapphyx.logeon.viewModelImpl.onboarding.OnboardingScreenEffect
import com.sapphyx.logeon.viewModelImpl.onboarding.OnboardingScreenState
import com.sapphyx.logeon.viewModelImpl.onboarding.OnboardingScreenViewModel
import kotlinx.coroutines.flow.SharedFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object OnboardingScreen :
    BaseScreen<OnboardingScreenState, OnboardingScreenAction, OnboardingScreenEffect, OnboardingScreenViewModel>() {

    @Composable
    override fun Screen(
        state: OnboardingScreenState,
        effect: SharedFlow<OnboardingScreenEffect>,
        processUiAction: (action: OnboardingScreenAction) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenHeadline()

            Spacer(modifier = Modifier.height(8.dp))

            ScreenSubtitle()

            Spacer(modifier = Modifier.height(24.dp))

            UsernameField(
                username = state.username,
                isAvailable = state.isUsernameAvailable,
                isError = state.usernameError != null,
                errorMessage = state.usernameError?.toErrorMessage(),
                isLoading = state.isLoading,
                processUiAction = processUiAction
            )

            Spacer(modifier = Modifier.height(8.dp))

            DisplayNameField(
                displayName = state.displayName,
                isLoading = state.isLoading,
                processUiAction = processUiAction
            )

            Spacer(modifier = Modifier.height(8.dp))

            BioField(
                bio = state.bio,
                isLoading = state.isLoading,
                processUiAction = processUiAction
            )

            Spacer(modifier = Modifier.height(8.dp))

            DateOfBirthField(
                dateOfBirthMillis = state.dateOfBirthMillis,
                isError = state.dateOfBirthError,
                isLoading = state.isLoading,
                processUiAction = processUiAction
            )

            Spacer(modifier = Modifier.height(8.dp))

            CountryField(
                selectedCountry = state.country,
                isError = state.countryError,
                isLoading = state.isLoading,
                processUiAction = processUiAction
            )

            GeneralError(state.generalError)

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                ContinueButton(
                    state = state,
                    processUiAction = processUiAction
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            SignOutButton(
                isLoading = state.isLoading,
                processUiAction = processUiAction
            )
        }
    }

    @Composable
    private fun ScreenHeadline() {
        Text(
            text = stringResource(R.string.onboarding_screen_headline),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
    }

    @Composable
    private fun ScreenSubtitle() {
        Text(
            text = stringResource(R.string.onboarding_screen_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }

    @Composable
    private fun UsernameField(
        username: String,
        isAvailable: Boolean,
        isError: Boolean,
        errorMessage: String?,
        isLoading: Boolean,
        processUiAction: (action: OnboardingScreenAction) -> Unit
    ) {
        val availableText = stringResource(R.string.onboarding_username_available)

        OutlinedTextField(
            value = username,
            onValueChange = { processUiAction(OnboardingScreenAction.UsernameChanged(it)) },
            label = { Text(stringResource(R.string.onboarding_username_label)) },
            prefix = { Text("@") },
            trailingIcon = if (isAvailable && !isError) {
                {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = availableText,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                null
            },
            isError = isError,
            supportingText = when {
                errorMessage != null -> {
                    { Text(text = errorMessage) }
                }

                isAvailable -> {
                    {
                        Text(
                            text = availableText,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                else -> null
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    private fun DisplayNameField(
        displayName: String,
        isLoading: Boolean,
        processUiAction: (action: OnboardingScreenAction) -> Unit
    ) {
        OutlinedTextField(
            value = displayName,
            onValueChange = { processUiAction(OnboardingScreenAction.DisplayNameChanged(it)) },
            label = { Text(stringResource(R.string.onboarding_display_name_label)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    private fun BioField(
        bio: String,
        isLoading: Boolean,
        processUiAction: (action: OnboardingScreenAction) -> Unit
    ) {
        OutlinedTextField(
            value = bio,
            onValueChange = { processUiAction(OnboardingScreenAction.BioChanged(it)) },
            label = { Text(stringResource(R.string.onboarding_bio_label)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next
            ),
            minLines = 2,
            maxLines = 3,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    private fun DateOfBirthField(
        dateOfBirthMillis: Long?,
        isError: Boolean,
        isLoading: Boolean,
        processUiAction: (action: OnboardingScreenAction) -> Unit
    ) {
        var showDatePicker by remember { mutableStateOf(false) }

        val formattedDate = remember(dateOfBirthMillis) {
            dateOfBirthMillis?.let { millis ->
                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                formatter.format(Date(millis))
            }.orEmpty()
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = formattedDate,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.onboarding_dob_label)) },
                placeholder = { Text(stringResource(R.string.onboarding_dob_select)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null
                    )
                },
                isError = isError,
                supportingText = if (isError) {
                    { Text(stringResource(R.string.onboarding_dob_required_error)) }
                } else {
                    null
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(enabled = !isLoading) { showDatePicker = true }
            )
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = dateOfBirthMillis,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                        utcTimeMillis <= System.currentTimeMillis()
                }
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                processUiAction(OnboardingScreenAction.DateOfBirthSelected(millis))
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text(stringResource(R.string.onboarding_dob_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.onboarding_dob_cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun CountryField(
        selectedCountry: String,
        isError: Boolean,
        isLoading: Boolean,
        processUiAction: (action: OnboardingScreenAction) -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }
        val countries = remember {
            Locale.getISOCountries()
                .map { code -> Locale("", code).displayCountry }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }
        ExposedDropdownMenuBox(
            expanded = expanded && !isLoading,
            onExpandedChange = { if (!isLoading) expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCountry,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.onboarding_country_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                isError = isError,
                supportingText = if (isError) {
                    { Text(stringResource(R.string.onboarding_country_required_error)) }
                } else {
                    null
                },
                enabled = !isLoading,
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded && !isLoading,
                onDismissRequest = { expanded = false }
            ) {
                countries.forEach { country ->
                    DropdownMenuItem(
                        text = { Text(country) },
                        onClick = {
                            processUiAction(OnboardingScreenAction.CountrySelected(country))
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun GeneralError(errorMessage: String?) {
        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun ContinueButton(
        state: OnboardingScreenState,
        processUiAction: (action: OnboardingScreenAction) -> Unit
    ) {
        Button(
            onClick = {
                processUiAction(
                    OnboardingScreenAction.CompleteOnboardingClick(
                        username = state.username,
                        displayName = state.displayName,
                        bio = state.bio,
                        dateOfBirthMillis = state.dateOfBirthMillis,
                        country = state.country
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.onboarding_complete_button))
        }
    }

    @Composable
    private fun SignOutButton(
        isLoading: Boolean,
        processUiAction: (action: OnboardingScreenAction) -> Unit
    ) {
        TextButton(
            onClick = { processUiAction(OnboardingScreenAction.SignOutClick) },
            enabled = !isLoading
        ) {
            Text(stringResource(R.string.onboarding_sign_out_button))
        }
    }
}
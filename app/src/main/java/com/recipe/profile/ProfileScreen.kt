package com.recipe.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.recipe.auth.presentation.components.AuthTextField
import com.recipe.ui.components.AnimatedLogo
import com.recipe.ui.theme.CreamBackground
import com.recipe.ui.theme.CreamBackgroundDeep
import com.recipe.ui.theme.ForestGreen
import com.recipe.ui.theme.SageGreen
import com.recipe.ui.theme.Terracotta

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLogout()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Profile",
            color = Terracotta,
            fontSize = 34.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Serif,
        )

        Text(
            text = "Your account details",
            color = ForestGreen.copy(alpha = 0.55f),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 22.dp),
        )

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Terracotta)
                }
            }

            state.user != null -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(CreamBackgroundDeep),
                        contentAlignment = Alignment.Center,
                    ) {
                        AnimatedLogo(size = 72.dp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = state.user?.name.orEmpty(),
                        color = ForestGreen,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                ProfileInfoCard(
                    icon = Icons.Outlined.Person,
                    label = "Name",
                    value = state.user?.name.orEmpty(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProfileInfoCard(
                    icon = Icons.Outlined.Email,
                    label = "Email",
                    value = state.user?.email.orEmpty(),
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (!state.isEditingPassword) {
                    OutlinedButton(
                        onClick = viewModel::onEditPasswordClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Terracotta,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Edit password",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                } else {
                    EditPasswordSection(
                        state = state,
                        onCurrentPasswordChange = viewModel::onCurrentPasswordChange,
                        onNewPasswordChange = viewModel::onNewPasswordChange,
                        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                        onSave = viewModel::updatePassword,
                        onCancel = viewModel::onCancelEditPassword,
                    )
                }

                if (state.message != null) {
                    Text(
                        text = state.message ?: "",
                        color = if (state.isErrorMessage) Terracotta else SageGreen,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 14.dp),
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = viewModel::logout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Terracotta,
                        contentColor = CreamBackground,
                    ),
                ) {
                    Text(
                        text = "Logout",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CreamBackgroundDeep)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SageGreen,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column {
            Text(
                text = label,
                color = ForestGreen.copy(alpha = 0.55f),
                fontSize = 12.sp,
            )
            Text(
                text = value,
                color = ForestGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun EditPasswordSection(
    state: ProfileUiState,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CreamBackgroundDeep)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = Terracotta,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Edit password",
                color = ForestGreen,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        AuthTextField(
            value = state.currentPassword,
            onValueChange = onCurrentPasswordChange,
            label = "Current password",
            isPassword = true,
            isError = state.currentPasswordError != null,
            supportingText = state.currentPasswordError,
        )

        Spacer(modifier = Modifier.height(10.dp))

        AuthTextField(
            value = state.newPassword,
            onValueChange = onNewPasswordChange,
            label = "New password",
            isPassword = true,
            isError = state.newPasswordError != null,
            supportingText = state.newPasswordError
                ?: "Min 6 chars, 1 capital, 1 special character",
        )

        Spacer(modifier = Modifier.height(10.dp))

        AuthTextField(
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Confirm new password",
            isPassword = true,
            isError = state.confirmPasswordError != null,
            supportingText = state.confirmPasswordError,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            ) {
                Text("Cancel", color = ForestGreen)
            }

            Button(
                onClick = onSave,
                enabled = !state.isUpdatingPassword,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Terracotta,
                    contentColor = CreamBackground,
                ),
            ) {
                if (state.isUpdatingPassword) {
                    CircularProgressIndicator(
                        color = CreamBackground,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Text("Save", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

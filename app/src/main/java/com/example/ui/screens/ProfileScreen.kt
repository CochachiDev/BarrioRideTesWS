package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.User
import com.example.domain.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUser: User,
    onSwitchRole: (UserRole) -> Unit,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card Header
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardSurface,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = EcoGreenContainer,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (currentUser.rol == UserRole.CONDUCTOR) Icons.Default.ElectricRickshaw else Icons.Default.Person,
                                contentDescription = null,
                                tint = EcoGreenDark,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "${currentUser.nombre} ${currentUser.apellido}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EcoGreenContainer
                        ) {
                            Text(
                                text = if (currentUser.rol == UserRole.CONDUCTOR) "CONDUCTOR ASIGNADO" else "VECINO RESIDENTE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EcoGreenDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Info Details
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.HomeWork, contentDescription = null, tint = SlateLight)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Ubicación / Residencia", fontSize = 11.sp, color = SlateLight)
                            Text(text = currentUser.residencia, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SlateDark)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = RoadGray)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = SlateLight)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Teléfono de contacto", fontSize = 11.sp, color = SlateLight)
                            Text(text = currentUser.telefono, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SlateDark)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = RoadGray)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = SlateLight)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Correo Electrónico", fontSize = 11.sp, color = SlateLight)
                            Text(text = currentUser.email, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SlateDark)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Quick Role Switcher Button
            OutlinedButton(
                onClick = {
                    val newRole = if (currentUser.rol == UserRole.CLIENTE) UserRole.CONDUCTOR else UserRole.CLIENTE
                    onSwitchRole(newRole)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("switch_role_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = EcoGreenDark)
            ) {
                Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (currentUser.rol == UserRole.CLIENTE) "CAMBIAR A MODO CONDUCTOR" else "CAMBIAR A MODO VECINO / CLIENTE",
                    fontWeight = FontWeight.Bold
                )
            }

            // Logout Button
            Button(
                onClick = {
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("logout_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SlateDark)
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "CERRAR SESIÓN", fontWeight = FontWeight.Bold)
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.domain.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRegisterScreen(
    initialRole: UserRole,
    onAuthSuccess: () -> Unit,
    onAuthenticate: (String, String, String, String, String, UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRole by remember { mutableStateOf(initialRole) }
    var nombre by remember { mutableStateOf(if (initialRole == UserRole.CLIENTE) "Carlos" else "Mateo") }
    var apellido by remember { mutableStateOf(if (initialRole == UserRole.CLIENTE) "Mendoza" else "Quispe") }
    var telefono by remember { mutableStateOf("+51 987 654 321") }
    var email by remember { mutableStateOf(if (initialRole == UserRole.CLIENTE) "carlos.vecino@inmobiliaria.com" else "mateo.conductor@inmobiliaria.com") }
    var residencia by remember { mutableStateOf(if (initialRole == UserRole.CLIENTE) "Manzana C - Lote 12" else "Base Central Trimotos") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro / Inicio de Sesión", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Role Tab Switcher
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedRole == UserRole.CLIENTE,
                    onClick = {
                        selectedRole = UserRole.CLIENTE
                        nombre = "Carlos"
                        email = "carlos.vecino@inmobiliaria.com"
                        residencia = "Manzana C - Lote 12"
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("tab_role_cliente")
                ) {
                    Text("Vecino (Cliente)")
                }
                SegmentedButton(
                    selected = selectedRole == UserRole.CONDUCTOR,
                    onClick = {
                        selectedRole = UserRole.CONDUCTOR
                        nombre = "Mateo"
                        email = "mateo.conductor@inmobiliaria.com"
                        residencia = "Base Central Trimotos"
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("tab_role_conductor")
                ) {
                    Text("Conductor")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (selectedRole == UserRole.CLIENTE) "Ingresa tus datos de vecino residente" else "Ingresa tus credenciales de conductor de trimoto",
                fontSize = 14.sp,
                color = SlateLight
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_input_name"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = apellido,
                onValueChange = { apellido = it },
                label = { Text("Apellido") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_input_lastname"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono de contacto") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_input_phone"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_input_email"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = residencia,
                onValueChange = { residencia = it },
                label = { Text(if (selectedRole == UserRole.CLIENTE) "Manzana / Lote o Torre / Apt" else "Unidad / Base asignada") },
                leadingIcon = { Icon(Icons.Default.HomeWork, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_input_residence"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onAuthenticate(nombre, apellido, telefono, email, residencia, selectedRole)
                    onAuthSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("auth_submit_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenDark)
            ) {
                Text(
                    text = "CONTINUAR A LA APLICACIÓN",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

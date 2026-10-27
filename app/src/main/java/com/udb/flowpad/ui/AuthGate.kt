package com.udb.flowpad.ui

import androidx.compose.ui.res.stringResource
import com.udb.flowpad.R

import android.app.Application
import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.udb.flowpad.ui.theme.FlowpadTheme

@Composable
fun AuthGate() {
    val context = LocalContext.current
    val auth = remember {
        runCatching {
            FirebaseApp.initializeApp(context)
            FirebaseAuth.getInstance()
        }.getOrNull()
    }
    var user by remember { mutableStateOf(auth?.currentUser) }
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { user = it.currentUser }
        auth?.addAuthStateListener(listener)
        onDispose { auth?.removeAuthStateListener(listener) }
    }
    FlowpadTheme(dark = false) {
        val current = user
        if (auth == null) {
            Surface(Modifier.fillMaxSize()) {
                Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.text_39), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.text_40))
                }
            }
        } else if (current == null) {
            LoginScreen(auth)
        } else key(current.uid) {
            val owner = remember { object : ViewModelStoreOwner { override val viewModelStore = ViewModelStore() } }
            DisposableEffect(owner) { onDispose { owner.viewModelStore.clear() } }
            val factory = remember(current.uid) { object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    FlowpadViewModel(context.applicationContext as Application, current.uid) as T
            } }
            val vm: FlowpadViewModel = viewModel(viewModelStoreOwner = owner, factory = factory)
            SignedInApp(vm, current.email.orEmpty(), onLogout = { auth.signOut() })
        }
    }
}

@Composable
private fun LoginScreen(auth: FirebaseAuth) {
    val context = LocalContext.current
    var register by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.imePadding().verticalScroll(rememberScrollState()).padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Spacer(Modifier.height(40.dp))
            Text(stringResource(R.string.text_22), style = MaterialTheme.typography.headlineLarge)
            Text(if (register) stringResource(R.string.label_68) else stringResource(R.string.label_69), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.text_41))
            OutlinedTextField(value = email, onValueChange = { email = it }, enabled = !busy,
                label = { Text(stringResource(R.string.text_42)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = password, onValueChange = { password = it }, enabled = !busy,
                label = { Text(stringResource(R.string.text_43)) }, singleLine = true, visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth())
            if (register) OutlinedTextField(value = repeat, onValueChange = { repeat = it }, enabled = !busy,
                label = { Text(stringResource(R.string.text_44)) }, singleLine = true, visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(enabled = !busy, modifier = Modifier.fillMaxWidth(), onClick = {
                error = when {
                    email.isBlank() || password.isBlank() -> context.getString(R.string.label_58)
                    !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> context.getString(R.string.label_59)
                    register && password.length < 6 -> context.getString(R.string.label_60)
                    register && password != repeat -> context.getString(R.string.label_61)
                    else -> null
                }
                if (error == null) {
                    busy = true
                    val task = if (register) auth.createUserWithEmailAndPassword(email.trim(), password)
                        else auth.signInWithEmailAndPassword(email.trim(), password)
                    task.addOnCompleteListener { result ->
                        busy = false
                        if (!result.isSuccessful) error = authError(context, result.exception)
                        else { password = ""; repeat = "" }
                    }
                }
            }) { Text(if (busy) stringResource(R.string.label_70) else if (register) stringResource(R.string.label_71) else stringResource(R.string.label_72)) }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            TextButton(enabled = !busy, onClick = { register = !register; error = null; password = ""; repeat = "" }) {
                Text(if (register) stringResource(R.string.label_73) else stringResource(R.string.label_74))
            }
            Text(stringResource(R.string.text_45),
                style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun authError(context: android.content.Context, error: Exception?): String = when (error) {
    is FirebaseNetworkException -> context.getString(R.string.label_62)
    is FirebaseAuthUserCollisionException -> context.getString(R.string.label_63)
    is FirebaseAuthWeakPasswordException -> context.getString(R.string.label_64)
    is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException -> context.getString(R.string.label_65)
    is FirebaseTooManyRequestsException -> context.getString(R.string.label_66)
    else -> context.getString(R.string.label_67)
}

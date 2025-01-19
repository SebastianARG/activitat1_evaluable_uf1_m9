# Si se quieren probar archivos del propio proyecto

## decrypt:
```batch
java com/didacysebas/KeyBreaker decrypt src/main/resources/mensajes/texto_cifrado.txt pwdfile=src/main/resources/mensajes/password.txt out=src/main/resources/mensajes/texto_descifrado.txt
```

## replace:
```batch
java com/didacysebas/KeyBreaker replace src/main/resources/mensajes/texto_cifrado2.txt pwdfile=src/main/resources/mensajes/password.txt out=src/main/resources/mensajes/texto_reemplazado.txt
```
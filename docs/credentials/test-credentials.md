# Credenciales Temporales de Prueba

⚠️ **IMPORTANTE: Estas son credenciales temporales solo para testing/desarrollo**

## Usuarios de Prueba

| Nombre | Número de Afiliado | Contraseña |
|--------|-------------------|------------|
| Juan Pérez | 12345 | password123 |
| María García | 67890 | mypass456 |
| Test User | TEST001 | test123 |
| Carlos López | AFIL001 | password2024 |

## Notas

- Estas credenciales son **SOLO PARA DESARROLLO**
- No usar en producción
- Cambiar antes del deploy final
- Son credenciales de prueba del sistema de autenticación

## Endpoint de Autenticación

```
POST https://backend-ipross-production.up.railway.app/api/auth/login
```

**Body:**
```json
{
  "numero_afiliado": "12345",
  "contraseña": "password123"
}
```

---

*Última actualización: Octubre 2025*

package co.com.jhompo.util;

public final class Messages {

    // Constructor privado para evitar la instanciación de la clase de utilidad
    private Messages() {}

    // ============================================
    // MENSAJES DE SEGURIDAD Y JWT
    // ============================================
    public static final class SECURITY {
        // Errores JWT
        public static final String INVALID_TOKEN = "Token JWT inválido";
        public static final String TOKEN_EXPIRED = "Token JWT expirado";
        public static final String TOKEN_MALFORMED = "Token JWT mal formado";
        public static final String TOKEN_SIGNATURE_INVALID = "Firma del token JWT inválida";
        public static final String TOKEN_NOT_PROVIDED = "Token JWT no proporcionado";

        // Autorización y Autenticación
        public static final String UNAUTHORIZED_ACCESS = "Acceso no autorizado. Por favor, inicie sesión.";
        public static final String FORBIDDEN_ACCESS = "Acceso prohibido. No tiene los permisos necesarios.";
        public static final String INVALID_CREDENTIALS = "Credenciales de inicio de sesión inválidas";
        public static final String LOGIN_SUCCESS = "Inicio de sesión exitoso";
        public static final String LOGOUT_SUCCESS = "Cierre de sesión exitoso";

        private SECURITY() {}
    }

    public static final class ROLE {
        // Éxito
        public static final String ADMIN = "ADMIN";
        public static final String ASESOR = "ASESOR";
        public static final String CLIENTE = "CLIENTE";
        public static final String CREATED_SUCCESS = "Rol creado exitosamente";
        public static final String UPDATED_SUCCESS = "Rol actualizado exitosamente";
        public static final String DELETED_SUCCESS = "Rol eliminado exitosamente";
        public static final String RETRIEVED_SUCCESS = "Roles obtenidos exitosamente";

        // Errores
        public static final String NOT_FOUND = "Rol no encontrado";
        public static final String ALREADY_EXISTS = "El rol ya existe";
        public static final String NAME_ALREADY_EXISTS = "Ya existe un rol con ese nombre";
        public static final String CREATION_FAILED = "Error al crear el rol";
        public static final String UPDATE_FAILED = "Error al actualizar el rol";
        public static final String DELETE_FAILED = "Error al eliminar el rol";
        public static final String IN_USE = "No se puede eliminar el rol porque está en uso";

        // Validaciones
        public static final String NAME_REQUIRED = "El nombre del rol es requerido";
        public static final String NAME_TOO_SHORT = "El nombre del rol debe tener al menos 2 caracteres";
        public static final String DESCRIPTION_REQUIRED = "La descripción del rol es requerida";
        public static final String INVALID_ROLE_ID = "ID de rol inválido";

        private ROLE() {}
    }

    // ============================================
    // MENSAJES DE APLICACIÓN DE PRÉSTAMOS
    // ============================================
    public static final class LOAN_APPLICATION {
        // Éxito
        public static final String CREATED_SUCCESS = "Solicitud de préstamo creada exitosamente";
        public static final String UPDATED_SUCCESS = "Solicitud de préstamo actualizada exitosamente";
        public static final String DELETED_SUCCESS = "Solicitud de préstamo eliminada exitosamente";
        public static final String FOUND_SUCCESS = "Solicitud(es) de préstamo encontrada(s) exitosamente";
        public static final String APPROVED_SUCCESS = "Solicitud de préstamo aprobada";
        public static final String REJECTED_SUCCESS = "Solicitud de préstamo rechazada";
        public static final String CANCELLED_SUCCESS = "Solicitud de préstamo cancelada";

        // Errores
        public static final String NOT_FOUND = "Solicitud de préstamo no encontrada";
        public static final String CREATION_FAILED = "Error al crear la solicitud de préstamo";
        public static final String UPDATE_FAILED = "Error al actualizar la solicitud de préstamo";
        public static final String DELETE_FAILED = "Error al eliminar la solicitud de préstamo";
        public static final String INVALID_STATUS_TRANSITION = "Transición de estado inválida para la solicitud";
        public static final String INSUFFICIENT_FUNDS = "El monto solicitado excede el límite de aprobación";
        public static final String DUPLICATE_ENTRY = "Ya existe una solicitud de préstamo con esta información";

        // Validaciones
        public static final String AMOUNT_REQUIRED = "El monto es requerido y debe ser mayor a 0";
        public static final String TERM_REQUIRED = "El plazo es requerido y debe ser un número entero positivo";

        private LOAN_APPLICATION() {}
    }

    // ============================================
    // MENSAJES DE ESTADOS
    // ============================================
    public static final class STATUS {
        public static final String CREATED_SUCCESS = "Estado creado exitosamente";
        public static final String FOUND_SUCCESS = "Estado(s) encontrado(s) exitosamente";
        public static final String NOT_FOUND = "Estado no encontrado";
        public static final String ALREADY_EXISTS = "El estado ya existe";

        private STATUS() {}
    }

    // ============================================
    // MENSAJES DE TIPOS DE APLICACIÓN
    // ============================================
    public static final class APPLICATION_TYPE {
        public static final String CREATED_SUCCESS = "Tipo de aplicación creado exitosamente";
        public static final String FOUND_SUCCESS = "Tipo(s) de aplicación encontrado(s) exitosamente";
        public static final String NOT_FOUND = "Tipo de aplicación no encontrado";
        public static final String ALREADY_EXISTS = "El tipo de aplicación ya existe";

        private APPLICATION_TYPE() {}
    }

    // ============================================
    // MENSAJES DE PERMISOS
    // ============================================
    public static final class PERMISSION {
        // Éxito
        public static final String ASSIGNED_SUCCESS = "Permiso asignado exitosamente";
        public static final String REVOKED_SUCCESS = "Permiso revocado exitosamente";
        public static final String RETRIEVED_SUCCESS = "Permisos obtenidos exitosamente";

        // Errores
        public static final String NOT_FOUND = "Permiso no encontrado";
        public static final String ALREADY_ASSIGNED = "El permiso ya está asignado";
        public static final String ASSIGNMENT_FAILED = "Error al asignar el permiso";
        public static final String REVOCATION_FAILED = "Error al revocar el permiso";
        public static final String INSUFFICIENT_PERMISSIONS = "Permisos insuficientes";

        private PERMISSION() {}
    }

    // ============================================
    // MENSAJES GENERALES/SISTEMA
    // ============================================
    public static final class SYSTEM {
        // Éxito
        public static final String OPERATION_SUCCESS = "Operación realizada exitosamente";
        public static final String DATA_RETRIEVED = "Datos obtenidos exitosamente";
        public static final String CACHE_CLEARED = "Caché limpiado exitosamente";

        // Errores
        public static final String INTERNAL_ERROR = "Error interno del servidor";
        public static final String DATABASE_ERROR = "Error de conexión a la base de datos";
        public static final String NETWORK_ERROR = "Error de conexión de red";
        public static final String SERVICE_UNAVAILABLE = "Servicio no disponible";
        public static final String TIMEOUT_ERROR = "Tiempo de espera agotado";
        public static final String VALIDATION_ERROR = "Error de validación";
        public static final String SERIALIZATION_ERROR = "Error de serialización";
        public static final String DESERIALIZATION_ERROR = "Error de deserialización";

        // Validaciones generales
        public static final String REQUIRED_FIELD = "Campo requerido";
        public static final String INVALID_FORMAT = "Formato inválido";
        public static final String INVALID_ID = "ID inválido";
        public static final String INVALID_DATE = "Fecha inválida";
        public static final String INVALID_RANGE = "Rango inválido";
        public static final String RESOURCE_NOT_FOUND = "Recurso no encontrado";
        public static final String DUPLICATE_ENTRY = "Entrada duplicada";

        private SYSTEM() {}
    }

    // ============================================
    // MENSAJES DE SOLICITUDES
    // ============================================
    public static final class REQUEST {
        // Éxito
        public static final String CREATED_SUCCESS = "Solicitud creada exitosamente";
        public static final String UPDATED_SUCCESS = "Solicitud actualizada exitosamente";
        public static final String PROCESSED_SUCCESS = "Solicitud procesada exitosamente";
        public static final String APPROVED_SUCCESS = "Solicitud aprobada exitosamente";
        public static final String REJECTED_SUCCESS = "Solicitud rechazada exitosamente";
        public static final String CANCELLED_SUCCESS = "Solicitud cancelada exitosamente";

        // Errores
        public static final String NOT_FOUND = "Solicitud no encontrada";
        public static final String ALREADY_PROCESSED = "La solicitud ya fue procesada";
        public static final String INVALID_STATUS = "Estado de solicitud inválido";
        public static final String CREATION_FAILED = "Error al crear la solicitud";
        public static final String UPDATE_FAILED = "Error al actualizar la solicitud";
        public static final String PROCESSING_FAILED = "Error al procesar la solicitud";

        // Validaciones
        public static final String TYPE_REQUIRED = "El tipo de solicitud es requerido";
        public static final String DESCRIPTION_REQUIRED = "La descripción es requerida";
        public static final String INVALID_REQUEST_TYPE = "Tipo de solicitud inválido";

        private REQUEST() {}
    }

    // ============================================
    // MENSAJES HTTP/API
    // ============================================
    public static final class HTTP {
        public static final String BAD_REQUEST = "Solicitud incorrecta";
        public static final String UNAUTHORIZED = "No autorizado";
        public static final String FORBIDDEN = "Prohibido";
        public static final String NOT_FOUND = "No encontrado";
        public static final String METHOD_NOT_ALLOWED = "Método no permitido";
        public static final String CONFLICT = "Conflicto";
        public static final String UNPROCESSABLE_ENTITY = "Entidad no procesable";
        public static final String INTERNAL_SERVER_ERROR = "Error interno del servidor";
        public static final String SERVICE_UNAVAILABLE = "Servicio no disponible";
        public static final String REQUEST_TIMEOUT = "Tiempo de solicitud agotado";

        private HTTP() {}
    }

}

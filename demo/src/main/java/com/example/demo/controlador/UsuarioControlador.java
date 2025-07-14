package com.example.demo.controlador;

import com.example.demo.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.example.demo.datos.*;
import com.example.demo.modelo.*;

import com.example.demo.services.EmailService;
import com.example.demo.services.GeminiService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/usuarios")
public class UsuarioControlador {

    @Autowired
    private  SedeDAO sedeDAO;

    @Autowired
    private CursoSedeDAO cursoSedeDAO;

    @Autowired
    private RecetaCalculadaDAO recetaCalculadaDAO;

    @Autowired
    private IngredienteDAO ingredienteDAO;

    @Autowired
    private IngredienteRecetaDAO ingredienteRecetaDAO;


    @Autowired
    private ComentarioValoracionDAO comentarioValoracionDAO;

    @Autowired
    private RecetaDAO recetaDAO;

    @Autowired
    private UsuarioDAO usuarioDAO;

    @Autowired
    private PasoRecetaDAO pasoRecetaDAO;

    @Autowired
    private EmailService emailService;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AlumnoDAO alumnoDAO;

    @Autowired
    private CursoDAO cursoDAO;

    @Autowired
    private CuentaCorrienteDAO cuentaCorrienteDAO;

    @Autowired
    private PagoDAO pagoDAO;

    @Autowired
    private AsistenciaDAO asistenciaDAO;

    @Autowired
    private MaterialDeClaseDAO materialDeClaseDAO;

//    @Autowired
//    private CursoDAO alumnoDAO;

    @PostMapping("/registro/iniciar")
    @Transactional
    public ResponseEntity<?> iniciarRegistro(@RequestBody Usuario usuarioParcial) {
        if (usuarioParcial.getEmail() == null || usuarioParcial.getAlias() == null) {
            return ResponseEntity.badRequest().body("Faltan campos obligatorios.");
        }

        Optional<Usuario> aliasExistente = usuarioDAO.getUsuarioByAlias(usuarioParcial.getAlias());
        Optional<Usuario> emailExistente = usuarioDAO.getUsuarioByEmail(usuarioParcial.getEmail());

        boolean aliasEnUso = aliasExistente.isPresent() && aliasExistente.get().isRegistroCompleto();
        boolean emailEnUso = emailExistente.isPresent() && emailExistente.get().isRegistroCompleto();

        if (aliasEnUso && emailEnUso) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Este correo y alias ya están en uso.");
        } else if (emailEnUso) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Este correo ya está registrado.");
        } else if (aliasEnUso) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Este alias ya está en uso.");
        }

// Si el email existe pero no se completó el registro, entonces se permite continuar,
// se vuelve a enviar el código y no bloquea el registro
        if (emailExistente.isPresent() && !emailExistente.get().isRegistroCompleto()) {
            Usuario existente = emailExistente.get();

            String nuevoCodigo = String.valueOf((int) (Math.random() * 900000) + 100000);
            existente.setCodigoRegistro(nuevoCodigo);
            existente.setFechaExpiracionCodigo(LocalDateTime.now().plusMinutes(30));

            usuarioDAO.actualizarUsuario(existente);
            emailService.enviarCodigoConfirmacion(existente.getEmail(), nuevoCodigo);

            return ResponseEntity.ok(existente);
        }

// Si no existe el email, crear nuevo usuario como antes
        Usuario nuevo = new Usuario();
        nuevo.setEmail(usuarioParcial.getEmail());
        nuevo.setAlias(usuarioParcial.getAlias());
        nuevo.setRegistroCompleto(false);
        nuevo.setActivo(false);

        String codigo = String.valueOf((int) (Math.random() * 900000) + 100000);
        nuevo.setCodigoRegistro(codigo);
        nuevo.setFechaExpiracionCodigo(LocalDateTime.now().plusMinutes(30));

        usuarioDAO.agregarUsuario(nuevo);
        emailService.enviarCodigoConfirmacion(nuevo.getEmail(), codigo);

        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @PostMapping("/registro/validar-codigo")
    @Transactional
    public ResponseEntity<String> validarCodigo(@RequestBody ConfirmacionRequest request) {
        if (request.getEmail() == null || request.getCodigo() == null) {
            return ResponseEntity.badRequest().body("Faltan datos");
        }

        Optional<Usuario> optionalUsuario = usuarioDAO.getUsuarioByEmail(request.getEmail());

        if (optionalUsuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Usuario usuario = optionalUsuario.get();

        if (usuario.getCodigoRegistro() == null || usuario.getFechaExpiracionCodigo() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Código no generado");
        }

        if (!usuario.getCodigoRegistro().equals(request.getCodigo())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Código incorrecto");
        }

        if (usuario.getFechaExpiracionCodigo().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE).body("Código expirado");
        }

        usuario.setCodigoRegistro(null);
        usuario.setFechaExpiracionCodigo(null);

        usuarioDAO.actualizarUsuario(usuario);

        return ResponseEntity.ok("Código validado con éxito");
    }

    @PostMapping("/registro/completar")
    @Transactional
    public ResponseEntity<?> completarRegistro(@RequestBody Usuario datosFinales) {
        if (datosFinales.getEmail() == null) {
            return ResponseEntity.badRequest().body("El email es obligatorio.");
        }

        Optional<Usuario> optionalUsuario = usuarioDAO.getUsuarioByEmail(datosFinales.getEmail());

        if (optionalUsuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró el usuario.");
        }

        Usuario usuario = optionalUsuario.get();

        if (Boolean.TRUE.equals(usuario.isRegistroCompleto())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El registro ya fue completado.");
        }

        if (datosFinales.getNombre() == null || datosFinales.getApellido() == null || datosFinales.getPassword() == null) {
            return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
        }

        usuario.setNombre(datosFinales.getNombre());
        usuario.setApellido(datosFinales.getApellido());
        usuario.setPassword(datosFinales.getPassword()); // Asegurate de hashearla si corresponde
        usuario.setRegistroCompleto(true);
        usuario.setActivo(true);

        Usuario actualizado = usuarioDAO.actualizarUsuario(usuario);

        if (actualizado == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el usuario.");
        }

        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario credenciales) {
        try {
            if (credenciales.getEmail() == null || credenciales.getPassword() == null) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Usuario> usuarioOpt = usuarioDAO.getUsuarioByEmail(credenciales.getEmail());

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();

                if (usuario.getPassword() != null && usuario.getPassword().equals(credenciales.getPassword())) {
                    if (usuario.isActivo() && usuario.isRegistroCompleto()) {
                        return ResponseEntity.ok(usuario);
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno: " + e.getMessage());
        }
    }

    @PostMapping("/recuperar/enviar-codigo")
    @Transactional
    public ResponseEntity<?> enviarCodigoRecuperacion(@RequestBody Usuario request) {
        if (request.getEmail() == null) {
            return ResponseEntity.badRequest().body("Email es obligatorio.");
        }

        Optional<Usuario> usuarioOpt = usuarioDAO.getUsuarioByEmail(request.getEmail());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        Usuario usuario = usuarioOpt.get();

        String codigo = String.valueOf((int)(Math.random() * 900000) + 100000);
        usuario.setCodigoRegistro(codigo);
        usuario.setFechaExpiracionCodigo(LocalDateTime.now().plusMinutes(30));

        usuarioDAO.actualizarUsuario(usuario);
        emailService.enviarCodigoConfirmacion(usuario.getEmail(), codigo);

        return ResponseEntity.ok("Código de recuperación enviado.");
    }

    @PostMapping("/recuperar/validar-codigo")
    @Transactional
    public ResponseEntity<?> validarCodigoRecuperacion(@RequestBody ConfirmacionRequest request) {
        if (request.getEmail() == null || request.getCodigo() == null) {
            return ResponseEntity.badRequest().body("Faltan datos.");
        }

        Optional<Usuario> optionalUsuario = usuarioDAO.getUsuarioByEmail(request.getEmail());

        if (optionalUsuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        Usuario usuario = optionalUsuario.get();

        if (usuario.getCodigoRegistro() == null || usuario.getFechaExpiracionCodigo() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se generó un código.");
        }

        if (!usuario.getCodigoRegistro().equals(request.getCodigo())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Código incorrecto.");
        }

        if (usuario.getFechaExpiracionCodigo().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.GONE).body("El código ha expirado.");
        }

        // No se borra el código ni se modifica el estado del usuario
        return ResponseEntity.ok("Código de recuperación validado correctamente.");
    }

    @PostMapping("/recuperar/cambiar-contrasenia")
    @Transactional
    public ResponseEntity<?> cambiarContrasenia(@RequestBody CambioContraseniaRequest request) {
        Optional<Usuario> optionalUsuario = usuarioDAO.getUsuarioByEmail(request.getEmail());

        if (optionalUsuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        Usuario usuario = optionalUsuario.get();
        usuario.setPassword(request.getNuevaContrasenia());
        usuarioDAO.agregarUsuario(usuario);

        return ResponseEntity.ok("Contraseña cambiada correctamente.");
    }

    @PostMapping("/verificar/alias")
    public ResponseEntity<?> verificarAlias(@RequestBody Map<String, String> body) {
        String alias = body.get("alias");
        if (alias == null || alias.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Alias no puede estar vacío");
        }

        Optional<Usuario> usuarioExistente = usuarioDAO.getUsuarioByAlias(alias.trim());
        if (usuarioExistente.isPresent()) {
            List<String> sugerencias = geminiService.obtenerSugerenciasAlias(alias.trim());

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Este alias esta en uso, sugerencias disponibles");
            response.put("sugerencias", sugerencias);

            return ResponseEntity.status(409).body(response);
        }

        Map<String, String> response = Map.of("mensaje", "Alias disponible");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verificar/email")
    public ResponseEntity<?> verificarEmail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email no puede estar vacío");
        }

        Optional<Usuario> usuarioExistente = usuarioDAO.getUsuarioByEmail(email.trim());
        if (usuarioExistente.isPresent()) {
            return ResponseEntity.status(409).body(Map.of("mensaje", "Este email ya está registrado"));
        }

        return ResponseEntity.ok(Map.of("mensaje", "Email disponible"));
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> obtenerUsuarioPorEmail(@PathVariable String email) {
        Optional<Usuario> usuarioOpt = usuarioDAO.getUsuarioByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        Usuario usuario = usuarioOpt.get();
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/{email}")
    @Transactional
    public ResponseEntity<?> actualizarUsuario(@PathVariable String email, @RequestBody Usuario datosActualizados) {
        Optional<Usuario> optionalUsuario = usuarioDAO.getUsuarioByEmail(email);

        if (optionalUsuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        Usuario usuario = optionalUsuario.get();

        // Actualiza solo los campos necesarios
        if (datosActualizados.getNombre() != null) usuario.setNombre(datosActualizados.getNombre());
        if (datosActualizados.getApellido() != null) usuario.setApellido(datosActualizados.getApellido());
        if (datosActualizados.getAlias() != null) usuario.setAlias(datosActualizados.getAlias());
        if (datosActualizados.getPassword() != null) usuario.setPassword(datosActualizados.getPassword()); // Considerar hash
        // Puedes agregar más campos si lo deseas

        usuarioDAO.actualizarUsuario(usuario);

        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/{email}/foto-perfil")
    @Transactional
    public ResponseEntity<?> subirFotoPerfil(@PathVariable String email, @RequestParam("foto") MultipartFile foto) {
        Optional<Usuario> usuarioOpt = usuarioDAO.getUsuarioByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        try {
            // Convertimos la imagen a Base64
            byte[] bytes = foto.getBytes();
            String imagenBase64 = Base64.getEncoder().encodeToString(bytes);

            // URL-encode para asegurar correcto envío
            String imagenBase64Encoded = URLEncoder.encode(imagenBase64, StandardCharsets.UTF_8);

            // Client-ID de tu cuenta Imgur
            String clientId = "e878a1a5e54a742"; // ← Cambiar por el tuyo si es necesario

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.imgur.com/3/image"))
                    .header("Authorization", "Client-ID " + clientId)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("image=" + imagenBase64Encoded))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(response.body());
                String urlImagen = jsonResponse.get("data").get("link").asText();

                // Guardamos la URL en el campo de foto de perfil
                usuario.setFotoPerfil(urlImagen);
                usuarioDAO.actualizarUsuario(usuario);

                return ResponseEntity.ok(Map.of(
                        "mensaje", "Foto de perfil actualizada correctamente",
                        "fotoPerfil", urlImagen
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al subir la imagen a Imgur: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando la imagen: " + e.getMessage());
        }
    }


    @GetMapping("/recetas/tarjetas")
    public ResponseEntity<List<Map<String, Object>>> obtenerRecetasParaTarjetas() {
        List<Receta> recetas = recetaDAO.getAllRecetas();

        List<Map<String, Object>> tarjetas = new ArrayList<>();

        for (Receta receta : recetas) {
            Map<String, Object> tarjeta = new HashMap<>();
            tarjeta.put("id", receta.getId());
            tarjeta.put("nombre", receta.getNombre());
            tarjeta.put("descripcion", receta.getDescripcion());
            tarjeta.put("tipo", receta.getTipo());
            tarjeta.put("porciones", receta.getPorciones());
            tarjeta.put("fechaCreacion", receta.getFechaCreacion());

            // Agrego tiempo, calificación y autor
            tarjeta.put("tiempo", receta.getTiempo());  // en minutos
            tarjeta.put("calificacion", receta.getCalificacion());

            // Autor (asumo que receta.getAutor() devuelve un objeto con getAlias())
            if (receta.getAutor() != null) {
                tarjeta.put("autor", receta.getAutor().getAlias());
            } else {
                tarjeta.put("autor", "Autor desconocido");
            }

            // Foto principal
            List<String> fotos = receta.getFotosPlato();
            if (fotos != null && !fotos.isEmpty()) {
                tarjeta.put("fotoPrincipal", fotos.get(0));
            } else {
                tarjeta.put("fotoPrincipal", null);
            }

            tarjetas.add(tarjeta);
        }

        return ResponseEntity.ok(tarjetas);
    }



    @PostMapping("/recetas/{id}/imagen")
    public ResponseEntity<String> agregarImagenReceta(@PathVariable Long id, @RequestParam("imagen") MultipartFile imagen) {
        Optional<Receta> recetaOpt = recetaDAO.getRecetaById(id);
        if (recetaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receta no encontrada");
        }

        Receta receta = recetaOpt.get();

        try {
            // Convertimos la imagen a Base64
            byte[] bytes = imagen.getBytes();
            String imagenBase64 = Base64.getEncoder().encodeToString(bytes);

            // URL-encode de la imagen Base64 para enviarla correctamente
            String imagenBase64Encoded = URLEncoder.encode(imagenBase64, StandardCharsets.UTF_8);

            // Preparamos la request a Imgur
            String clientId = "e878a1a5e54a742"; // <-- REEMPLAZAR por tu Client-ID real

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.imgur.com/3/image"))
                    .header("Authorization", "Client-ID " + clientId)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("image=" + imagenBase64Encoded))
                    .build();

            // Ejecutamos la request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(response.body());
                String urlImagen = jsonResponse.get("data").get("link").asText();

                List<String> fotos = receta.getFotosPlato();
                if (fotos == null) {
                    fotos = new ArrayList<>();
                    receta.setFotosPlato(fotos);
                }
                fotos.add(urlImagen);

                // Actualizo la receta con la nueva lista de fotos
                recetaDAO.actualizarReceta(receta);

                return ResponseEntity.ok("Imagen agregada correctamente: " + urlImagen);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al subir la imagen a Imgur: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando la imagen: " + e.getMessage());
        }
    }

    @GetMapping("/recetas/buscar/{nombre}/{alias}")
    public ResponseEntity<List<Receta>> buscarRecetasPorNombreYAlias(
            @PathVariable String nombre,
            @PathVariable String alias) {

        if (nombre.isEmpty() || alias.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Receta> recetasFiltradas = recetaDAO.getRecetasByNombreYAlias(nombre, alias);
        return ResponseEntity.ok(recetasFiltradas);
    }

    @GetMapping("/recetas/{id}")
    public ResponseEntity<Receta> getRecetaPorId(@PathVariable Long id) {
        Optional<Receta> recetaOpt = recetaDAO.getRecetaById(id);
        if (recetaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(recetaOpt.get());
    }

    @PostMapping("/recetas/{id}/comentarios")
    public ResponseEntity<?> crearValoracion(@PathVariable Long id, @RequestBody ComentarioValoracion valoracion) {
        valoracion.setFecha(LocalDateTime.now());

        if (valoracion == null || valoracion.getUsuario() == null || valoracion.getUsuario().getId() == null) {
            return ResponseEntity.badRequest().body("Datos incompletos para la valoración.");
        }

        if (valoracion.getPuntaje() != null && (valoracion.getPuntaje() < 1 || valoracion.getPuntaje() > 5)) {
            return ResponseEntity.badRequest().body("El puntaje debe estar entre 1 y 5.");
        }

        Optional<Receta> recetaOpt = recetaDAO.getRecetaById(id);
        if (recetaOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("La receta no existe.");
        }

        Long usuarioId = valoracion.getUsuario().getId();
        Optional<Usuario> usuarioOpt = usuarioDAO.getUsuarioById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("El usuario no existe.");
        }

        valoracion.setUsuario(usuarioOpt.get());
        valoracion.setReceta(recetaOpt.get());

        ComentarioValoracion nuevaValoracion = comentarioValoracionDAO.agregarComentarioValoracion(valoracion);

        // Recalcular y actualizar calificación promedio en receta
        Receta receta = recetaOpt.get();

        // Actualizar lista de comentarios con la nueva valoración agregada
        receta.getComentarios().add(nuevaValoracion);

        // Calcular promedio redondeado a 1 decimal
        double promedio = receta.getComentarios().stream()
                .map(ComentarioValoracion::getPuntaje)
                .filter(Objects::nonNull)
                .mapToDouble(puntaje -> puntaje != null ? puntaje : 0.0)
                .average()
                .orElse(0.0);

// Redondear a 1 decimal
        double promedioRedondeado = Math.round(promedio * 10.0) / 10.0;

        receta.setCalificacion(promedioRedondeado);
        recetaDAO.actualizarReceta(receta);

        return ResponseEntity.ok(nuevaValoracion);
    }

    @GetMapping("/{id}/favoritos")
    public ResponseEntity<List<Receta>> getRecetasFavoritasPorUsuarioId(@PathVariable Long id) {
        List<Receta> favoritas = recetaDAO.getRecetasFavoritasPorUsuarioId(id);
        if (favoritas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(favoritas);
    }

    @PostMapping("/{id}/favoritos/{recetaId}")
    public ResponseEntity<String> agregarRecetaAFavoritos(
            @PathVariable("id") Long id,
            @PathVariable("recetaId") Long recetaId) {

        // Validar que el id sea válido
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("Id inválido");
        }

        // Buscar usuario por id
        Optional<Usuario> usuarioOpt = usuarioDAO.getUsuarioById(id);
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con id: " + id);
        }
        Usuario usuario = usuarioOpt.get();

        // Buscar receta por id
        Optional<Receta> optReceta = recetaDAO.getRecetaById(recetaId);
        if (optReceta.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Receta receta = optReceta.get();

        // Inicializar lista de recetas favoritas si es null
        if (usuario.getRecetasFavoritas() == null) {
            usuario.setRecetasFavoritas(new ArrayList<>());
        }

        // Verificar si la receta ya está en favoritos
        if (usuario.getRecetasFavoritas().contains(receta)) {
            return ResponseEntity.badRequest().body("La receta ya está en favoritos");
        }

        // Agregar la receta a favoritos
        usuario.getRecetasFavoritas().add(receta);

        // Inicializar lista de usuarios que favoritaron la receta si es null
        if (receta.getUsuariosQueLaFavoritaron() == null) {
            receta.setUsuariosQueLaFavoritaron(new ArrayList<>());
        }

        // Agregar usuario a la lista de usuarios que favoritaron la receta, si no está ya
        if (!receta.getUsuariosQueLaFavoritaron().contains(usuario)) {
            receta.getUsuariosQueLaFavoritaron().add(usuario);
        }

        // Guardar los cambios en el usuario
        usuarioDAO.agregarUsuario(usuario);

        return ResponseEntity.ok("Receta agregada a favoritos correctamente");
    }

    @DeleteMapping("/{id}/favoritos/{recetaId}")
    public ResponseEntity<String> eliminarRecetaDeFavoritos(
            @PathVariable("id") Long id,              // ID del usuario
            @PathVariable("recetaId") Long recetaId   // ID de la receta
    ) {
        // Validar ID del usuario
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID de usuario inválido");
        }

        // Buscar usuario por ID
        Optional<Usuario> usuarioOpt = usuarioDAO.getUsuarioById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado con ID: " + id);
        }
        Usuario usuario = usuarioOpt.get();

        // Buscar receta por ID
        Optional<Receta> recetaOpt = recetaDAO.getRecetaById(recetaId);
        if (recetaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receta no encontrada con ID: " + recetaId);
        }
        Receta receta = recetaOpt.get();

        // Remover receta de la lista de favoritos del usuario
        if (usuario.getRecetasFavoritas() != null && usuario.getRecetasFavoritas().contains(receta)) {
            usuario.getRecetasFavoritas().remove(receta);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La receta no está en favoritos del usuario");
        }

        // Remover usuario de la lista de usuarios que favoritaron la receta
        if (receta.getUsuariosQueLaFavoritaron() != null) {
            receta.getUsuariosQueLaFavoritaron().remove(usuario);
        }

        // Guardar los cambios
        usuarioDAO.agregarUsuario(usuario);

        return ResponseEntity.ok("Receta eliminada de favoritos correctamente");
    }



    @GetMapping("/conFavorito/{usuarioId}")
    public ResponseEntity<List<Map<String, Object>>> obtenerRecetasConFavorito(@PathVariable Long usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioDAO.getUsuarioById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Usuario usuario = usuarioOpt.get();

        List<Receta> recetas = recetaDAO.getAllRecetas();
        List<Map<String, Object>> listaConFavorito = new ArrayList<>();

        for (Receta receta : recetas) {
            // Convertir Receta a Map (puede ser manual o con ObjectMapper)
            Map<String, Object> recetaMap = new HashMap<>();
            recetaMap.put("id", receta.getId());
            recetaMap.put("nombre", receta.getNombre());
            recetaMap.put("autor", receta.getAutor());  // Asegurate que esto serialice bien o adapta
            recetaMap.put("fotoPrincipal", receta.getFotoPrincipal());
            recetaMap.put("calificacion", receta.getCalificacion());
            recetaMap.put("porciones", receta.getPorciones());
            recetaMap.put("tiempo", receta.getTiempo());
            recetaMap.put("tipo", receta.getTipo());
            recetaMap.put("ingredientes", receta.getIngredientes());

            // Agregás el campo extra para indicar si es favorito o no para el usuario
            boolean esFavorita = usuario.getRecetasFavoritas() != null && usuario.getRecetasFavoritas().contains(receta);
            recetaMap.put("esFavorita", esFavorita);

            listaConFavorito.add(recetaMap);
        }

        return ResponseEntity.ok(listaConFavorito);
    }

    @GetMapping("/listado")
    public List<Ingrediente> listarIngredientes() {
        return ingredienteDAO.getAllIngredientes();
    }

    @GetMapping("/{id}/ingredientes-solo")
    public ResponseEntity<List<Ingrediente>> getIngredientesSolo(@PathVariable Long id) {
        List<Ingrediente> ingredientes = ingredienteRecetaDAO.getIngredientesDeReceta(id);
        return ResponseEntity.ok(ingredientes);
    }

    @GetMapping("/recetas/ordenadas/alfabeticamente")
    public ResponseEntity<List<Receta>> obtenerRecetasOrdenadasAlfabeticamente() {
        List<Receta> recetas = recetaDAO.getAllRecetas();

        if (recetas == null || recetas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        recetas.sort(Comparator.comparing(Receta::getNombre, String.CASE_INSENSITIVE_ORDER));

        return ResponseEntity.ok(recetas);
    }

    @GetMapping("/recetas/ordenadas/porFecha")
    public ResponseEntity<List<Receta>> obtenerRecetasOrdenadasPorFechaDesc() {
        List<Receta> recetas = recetaDAO.getRecetasOrdenadasPorFechaDesc();

        if (recetas == null || recetas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(recetas);
    }

    @GetMapping("/recetas/ordenadas/porFechaAsc")
    public ResponseEntity<List<Receta>> obtenerRecetasOrdenadasPorFechaAsc() {
        List<Receta> recetas = recetaDAO.getRecetasOrdenadasPorFechaAsc();

        if (recetas == null || recetas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(recetas);
    }

    @PostMapping("/recetas/{id}/guardar-escalada")
    public ResponseEntity<?> guardarRecetaCalculada(@PathVariable Long id, @RequestBody RecetaCalculada recetaBody) {
        if (recetaBody == null || recetaBody.getUsuario() == null || recetaBody.getUsuario().getId() == null) {
            return ResponseEntity.badRequest().body("Datos incompletos.");
        }

        // Validar receta original
        Optional<Receta> recetaOpt = recetaDAO.getRecetaById(id);
        if (recetaOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("La receta original no existe.");
        }

        // Validar usuario
        Long usuarioId = recetaBody.getUsuario().getId();
        Optional<Usuario> usuarioOpt = usuarioDAO.getUsuarioById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("El usuario no existe.");
        }

        recetaBody.setUsuario(usuarioOpt.get());
        recetaBody.setRecetaOriginal(recetaOpt.get());
        recetaBody.setFechaCreacion(LocalDateTime.now());

        RecetaCalculada nueva = recetaCalculadaDAO.guardarRecetaCalculada(recetaBody);

        return ResponseEntity.ok(nueva);
    }

    @DeleteMapping("/recetas-calculadas/{id}")
    public ResponseEntity<?> borrarRecetaCalculadaPorId(@PathVariable Long id) {
        Optional<RecetaCalculada> recetaOpt = recetaCalculadaDAO.getRecetaCalculadaById(id);
        if (recetaOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("La receta calculada no existe.");
        }

        recetaCalculadaDAO.borrarRecetaCalculada(recetaOpt.get());

        return ResponseEntity.ok("Receta calculada eliminada correctamente.");
    }

    @GetMapping("/{id}/recetas-calculadas2")
    public ResponseEntity<List<RecetaCalculada>> getRecetasCalculadasPorUsuario2(@PathVariable Long id) {
        List<RecetaCalculada> recetas = recetaCalculadaDAO.getRecetasCalculadasByUsuarioId(id);
        return ResponseEntity.ok(recetas);
    }

    @GetMapping("/{id}/recetas-calculadas")
    public ResponseEntity<List<RecetaCalculadaDTO>> getRecetasCalculadasPorUsuario(@PathVariable Long id) {
        List<RecetaCalculada> recetas = recetaCalculadaDAO.getRecetasCalculadasByUsuarioId(id);
        List<RecetaCalculadaDTO> resultado = recetas.stream()
                .map(RecetaCalculadaDTO::new)
                .toList();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/recetas-calculadas1/{id}")
    public ResponseEntity<RecetaCalculadaDTO> getRecetaCalculadaPorId(@PathVariable Long id) {
        Optional<RecetaCalculada> recetaOpt = recetaCalculadaDAO.getRecetaCalculadaById(id);
        if (recetaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        RecetaCalculadaDTO resultado = new RecetaCalculadaDTO(recetaOpt.get());
        return ResponseEntity.ok(resultado);
    }


    @GetMapping("/recetas/porUsuario/{alias}")
    public ResponseEntity<List<Receta>> buscarRecetasPorAlias(@PathVariable("alias") String alias) {
        if (alias == null || alias.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Receta> recetasFiltradas = recetaDAO.getRecetasByAliasUsuario(alias.trim());

        if (recetasFiltradas == null || recetasFiltradas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(recetasFiltradas);
    }

    @DeleteMapping("/recetas/eliminar/{alias}/{idReceta}")
    public ResponseEntity<Void> eliminarRecetaPorAlias(
            @PathVariable String alias,
            @PathVariable Long idReceta) {

        Optional<Receta> recetaOpt = recetaDAO.getRecetaById(idReceta);

        if (recetaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Receta receta = recetaOpt.get();

        if (receta.getAutor() == null || !receta.getAutor().getAlias().equalsIgnoreCase(alias)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        recetaDAO.eliminarReceta(receta.getId());
        return ResponseEntity.ok().build();
    }


    @PostMapping("/crearReceta/{alias}")
    public ResponseEntity<?> crearRecetaPorAlias(@PathVariable String alias, @RequestBody Receta receta) {
        Optional<Usuario> usuarioOpt = usuarioDAO.getUsuarioByAlias(alias);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario con alias '" + alias + "' no encontrado.");
        }

        receta.setAutor(usuarioOpt.get());
        receta.setFechaCreacion(java.time.LocalDateTime.now());
        Receta recetaGuardada = recetaDAO.agregarReceta(receta);

        return ResponseEntity.ok(recetaGuardada);
    }

    @PutMapping("/actualizarReceta/{id}")
    public ResponseEntity<?> actualizarReceta(@PathVariable Long id, @RequestBody Receta recetaActualizada) {
        Optional<Receta> recetaExistenteOpt = recetaDAO.getRecetaById(id);

        if (recetaExistenteOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No se encontró una receta con ID " + id);
        }

        Receta recetaExistente = recetaExistenteOpt.get();

        // Solo actualizamos si el campo no viene null
        if (recetaActualizada.getNombre() != null)
            recetaExistente.setNombre(recetaActualizada.getNombre());

        if (recetaActualizada.getDescripcion() != null)
            recetaExistente.setDescripcion(recetaActualizada.getDescripcion());

        if (recetaActualizada.getTiempo() != null)
            recetaExistente.setTiempo(recetaActualizada.getTiempo());

        if (recetaActualizada.getTipo() != null)
            recetaExistente.setTipo(recetaActualizada.getTipo());

        if (recetaActualizada.getPasos() != null && !recetaActualizada.getPasos().isEmpty())
            recetaExistente.setPasos(recetaActualizada.getPasos());

        if (recetaActualizada.getIngredientes() != null && !recetaActualizada.getIngredientes().isEmpty())
            recetaExistente.setIngredientes(recetaActualizada.getIngredientes());

        // Actualizamos la fecha
        recetaExistente.setFechaCreacion(java.time.LocalDateTime.now());

        Receta recetaModificada = recetaDAO.actualizarReceta(recetaExistente);

        return ResponseEntity.ok(recetaModificada);
    }

    @GetMapping("/por-ingredientes")
    public ResponseEntity<List<Receta>> buscarPorIngredientes(
            @RequestParam List<String> ingredientes) {

        List<Receta> recetas = recetaDAO.getRecetasConIngredientesMinimos(ingredientes);
        return ResponseEntity.ok(recetas);
    }

    @GetMapping("/recetas-calculadas/{usuarioId}")
    public ResponseEntity<List<RecetaCalculadaDTO>> obtenerRecetasCalculadasPorUsuarioId(@PathVariable Long usuarioId) { // <-- Cambia el tipo de retorno

        // 1. Obtienes las entidades como antes
        List<RecetaCalculada> recetasCalculadas =
                recetaCalculadaDAO.getRecetasCalculadasByUsuarioId(usuarioId);

        List<RecetaCalculadaDTO> responseDTOs = recetasCalculadas.stream()
                .map(RecetaCalculadaDTO::new) // Llama al constructor RecetaCalculadaDTO(receta) para cada elemento
                .collect(Collectors.toList());

        // 3. Devuelve la lista de DTOs
        return ResponseEntity.ok(responseDTOs);
    }


    @GetMapping("/ingredientes-json/receta/{recetaId}")
    public List<IngredienteDTO> getIngredientesJsonPorReceta(@PathVariable Long recetaId) {
        Optional<RecetaCalculada> recetaOptional = recetaCalculadaDAO.getRecetaCalculadaById(recetaId);
        List<IngredienteDTO> ingredientesDTO = new ArrayList<>();

        if (recetaOptional.isPresent()) {
            RecetaCalculada receta = recetaOptional.get();
            String json = receta.getIngredientesEscaladosJson();

            System.out.println("JSON recibido: " + json);  // Log para debug

            if (json != null && !json.isEmpty()) {
                try {
                    List<Map<String, Object>> listaIngredientes = objectMapper.readValue(
                            json,
                            new TypeReference<List<Map<String, Object>>>() {}
                    );

                    for (Map<String, Object> ingrediente : listaIngredientes) {
                        // Campos a nivel superior
                        String cantidad = ingrediente.get("cantidad") != null ? ingrediente.get("cantidad").toString() : "";
                        String unidad = ingrediente.get("unidad") != null ? ingrediente.get("unidad").toString() : "";

                        // Obtener submapa 'ingrediente'
                        Map<String, Object> subIngrediente = (Map<String, Object>) ingrediente.get("ingrediente");
                        String nombre = "";
                        String imagenUrl = "";

                        if (subIngrediente != null) {
                            nombre = subIngrediente.get("nombre") != null ? subIngrediente.get("nombre").toString() : "";
                            imagenUrl = subIngrediente.get("imagenUrl") != null ? subIngrediente.get("imagenUrl").toString() : "";
                        }

                        System.out.printf("Ingrediente parseado: nombre='%s', cantidad='%s', unidad='%s', imagenUrl='%s'%n",
                                nombre, cantidad, unidad, imagenUrl);

                        ingredientesDTO.add(new IngredienteDTO(nombre, cantidad, unidad, imagenUrl));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("No se encontró RecetaCalculada con id: " + recetaId);
        }

        return ingredientesDTO;
    }

    @PostMapping("/recetas/{id}/ingredientes")
    public ResponseEntity<String> agregarIngredientesReceta(
            @PathVariable Long id,
            @RequestBody List<IngredienteReceta> ingredientesNuevos) {

        Optional<Receta> recetaOpt = recetaDAO.getRecetaById(id);
        if (recetaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receta no encontrada");
        }

        if (ingredientesNuevos == null || ingredientesNuevos.isEmpty()) {
            return ResponseEntity.badRequest().body("La lista de ingredientes nuevos no puede estar vacía");
        }

        Receta receta = recetaOpt.get();

        List<IngredienteReceta> ingredientesActuales = receta.getIngredientes();
        if (ingredientesActuales == null) {
            ingredientesActuales = new ArrayList<>();
        }

        // Asociar cada ingrediente con la receta y agregarlos a la lista actual
        for (IngredienteReceta ingrediente : ingredientesNuevos) {
            ingrediente.setReceta(receta); // relación JPA importante
            ingredientesActuales.add(ingrediente);
        }

        receta.setIngredientes(ingredientesActuales);

        try {
            recetaDAO.actualizarReceta(receta);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar los ingredientes: " + e.getMessage());
        }

        return ResponseEntity.ok("Ingredientes agregados correctamente");
    }

    @PostMapping("/recetas/{id}/pasos")
    public ResponseEntity<String> agregarPasosReceta(
            @PathVariable Long id,
            @RequestBody List<PasoReceta> pasosNuevos) {

        Optional<Receta> recetaOpt = recetaDAO.getRecetaById(id);
        if (recetaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Receta no encontrada");
        }

        if (pasosNuevos == null || pasosNuevos.isEmpty()) {
            return ResponseEntity.badRequest().body("La lista de pasos no puede estar vacía");
        }

        Receta receta = recetaOpt.get();

        try {
            for (PasoReceta paso : pasosNuevos) {
                paso.setReceta(receta); // Relación importante para persistencia
                pasoRecetaDAO.agregarPaso(paso); // Guardar uno por uno
            }
            return ResponseEntity.ok("Pasos agregados correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar los pasos: " + e.getMessage());
        }
    }

    @PostMapping("/crearAlumno1")
    @Transactional
    public ResponseEntity<?> crearAlumno(@RequestBody Alumno alumnoParcial) {
        // Validar que venga el ID del usuario asociado (supongo que lo mandás en el JSON)
        if (alumnoParcial.getUsuario() == null || alumnoParcial.getUsuario().getId() == null) {
            return ResponseEntity.badRequest().body("El ID de usuario es obligatorio.");
        }

        Long idUsuario = alumnoParcial.getUsuario().getId();

        // Buscar el usuario en base de datos
        Optional<Usuario> optionalUsuario = usuarioDAO.getUsuarioById(idUsuario);

        if (optionalUsuario.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario con ID " + idUsuario + " no encontrado.");
        }

        Usuario usuario = optionalUsuario.get();

        // Validar si ya tiene un alumno asociado
        if (usuario.getAlumno() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Este usuario ya tiene un alumno asociado.");
        }

        // Crear nueva entidad Alumno y asignar campos que vienen en alumnoParcial
        Alumno alumno = new Alumno();

        // Copiás aquí los campos que quieras permitir modificar
        alumno.setNombreCompleto(alumnoParcial.getNombreCompleto());
        alumno.setNumTarjetaCredito(alumnoParcial.getNumTarjetaCredito());
        alumno.setFechaCaducidad(alumnoParcial.getFechaCaducidad());
        alumno.setCodigoSeguridad(alumnoParcial.getCodigoSeguridad());

        // Asociar el usuario al alumno (usuario ya viene "attached" porque lo recuperaste con DAO)
        alumno.setUsuario(usuario);

        // Guardar alumno
        alumnoDAO.agregarAlumno(alumno);

        // Crear cuenta corriente automáticamente para el alumno
        CuentaCorriente cuentaCorriente = new CuentaCorriente();
        cuentaCorriente.setAlumno(alumno);
        cuentaCorriente.setTotalFacturas(0.0f); // Inicialmente sin facturas
        cuentaCorrienteDAO.guardarCuentaCorriente(cuentaCorriente);

        // Actualizar usuario para reflejar la relación (bidireccional)
        usuario.setAlumno(alumno);
        usuario.setEsAlumno(true);
        usuarioDAO.actualizarUsuario(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(alumno);
    }


    @PostMapping("/actualizarAlumnoDatosFinales")
    @Transactional
    public ResponseEntity<?> actualizarDatosFinalesAlumno(
            @RequestParam("idUsuario") Long idUsuario,
            @RequestParam("numeroTramite") String numeroTramite,
            @RequestParam("imagenDniFrente") MultipartFile imagenDniFrente,
            @RequestParam("imagenDniDorso") MultipartFile imagenDniDorso
    ) {
        // Buscar usuario
        Optional<Usuario> optionalUsuario = usuarioDAO.getUsuarioById(idUsuario);
        if (optionalUsuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario con ID " + idUsuario + " no encontrado.");
        }

        Usuario usuario = optionalUsuario.get();

        Alumno alumno = usuario.getAlumno();
        if (alumno == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El usuario no tiene un alumno asociado para actualizar.");
        }

        try {
            // Subir imágenes a Imgur
            String urlFrente = subirImagenAImgur(imagenDniFrente);
            String urlDorso = subirImagenAImgur(imagenDniDorso);

            // Actualizar datos del alumno
            alumno.setNumeroTramiteDNI(numeroTramite);
            alumno.setDniFrenteUrl(urlFrente);
            alumno.setDniDorsoUrl(urlDorso);

            // Guardar cambios
            alumnoDAO.actualizarAlumno(alumno);

            return ResponseEntity.ok("Datos finales del alumno actualizados correctamente.");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar las imágenes: " + e.getMessage());
        }
    }

    private String subirImagenAImgur(MultipartFile foto) throws IOException, InterruptedException {
        // Convertimos la imagen a Base64
        byte[] bytes = foto.getBytes();
        String imagenBase64 = Base64.getEncoder().encodeToString(bytes);

        // URL-encode para asegurar correcto envío
        String imagenBase64Encoded = URLEncoder.encode(imagenBase64, StandardCharsets.UTF_8);

        // Client-ID de tu cuenta Imgur
        String clientId = "e878a1a5e54a742"; // Cambiar por el tuyo si es necesario

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.imgur.com/3/image"))
                .header("Authorization", "Client-ID " + clientId)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("image=" + imagenBase64Encoded))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response.body());
            return jsonResponse.get("data").get("link").asText();
        } else {
            throw new IOException("Error al subir la imagen a Imgur: " + response.body());
        }
    }


    @GetMapping("/usuarios/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioDAO.getUsuarioById(id);
        if (usuarioOpt.isPresent()) {
            return ResponseEntity.ok(usuarioOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }




    @GetMapping("/cursos/tarjetas")
    public ResponseEntity<List<Map<String, Object>>> obtenerCursosParaTarjetas() {
        List<Curso> cursos = cursoDAO.getAllCursos(); // Asegurate de tener este método en tu DAO

        List<Map<String, Object>> tarjetas = new ArrayList<>();

        for (Curso curso : cursos) {
            Map<String, Object> tarjeta = new HashMap<>();
            tarjeta.put("id", curso.getId());
            tarjeta.put("nombre", curso.getNombre());
            tarjeta.put("descripcion", curso.getDescripcion());
            tarjeta.put("fotoPrincipal", curso.getFotosUrl());
            tarjeta.put("modalidad", curso.getModalidad() != null ? curso.getModalidad().toString() : "No definida");
            tarjeta.put("importe", curso.getImporte());
            tarjeta.put("duracion", curso.getDuracion());
            tarjeta.put("estado", curso.getEstadoCurso() != null ? curso.getEstadoCurso().toString() : "No definido");
            tarjeta.put("fechaInicio", curso.getFechaInicio());
            tarjeta.put("fechaFin", curso.getFecha_fin());

            tarjetas.add(tarjeta);
        }

        return ResponseEntity.ok(tarjetas);
    }


    @GetMapping("/{id}/sedes")
    public ResponseEntity<List<Map<String, Object>>> obtenerSedesPorCurso(@PathVariable Long id) {
        List<CursoSede> cursoSedes = cursoSedeDAO.getByCursoId(id);

        List<Map<String, Object>> resultado = cursoSedes.stream().map(cs -> {
            Map<String, Object> datos = new HashMap<>();
            datos.put("nombreSede", cs.getSede().getNombre());
            datos.put("vacantesDisponibles", cs.getVacantesDisponibles());
            return datos;
        }).toList();

        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/curso-sede/crear")
    @Transactional
    public ResponseEntity<?> crearCursoSede(@RequestBody CursoSedeDTO cursoSedeDTO) {
        // Validar datos de entrada
        if (cursoSedeDTO.getCursoId() == null || cursoSedeDTO.getSedeId() == null) {
            return ResponseEntity.badRequest().body("Los IDs de curso y sede son obligatorios.");
        }

        if (cursoSedeDTO.getCursadaHorarioDia() == null || cursoSedeDTO.getCursadaHorarioDia().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El horario y dÃ­a de cursada es obligatorio.");
        }

        if (cursoSedeDTO.getCapacidad() <= 0) {
            return ResponseEntity.badRequest().body("La capacidad debe ser mayor a 0.");
        }

        // Buscar el curso
        Optional<Curso> cursoOpt = cursoDAO.getCursoById(cursoSedeDTO.getCursoId());
        if (cursoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Curso no encontrado con ID: " + cursoSedeDTO.getCursoId());
        }

        // Buscar la sede
        Optional<Sede> sedeOpt = sedeDAO.getSedeById(cursoSedeDTO.getSedeId());
        if (sedeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sede no encontrada con ID: " + cursoSedeDTO.getSedeId());
        }

        // Crear el CursoSede
        CursoSede cursoSede = new CursoSede();
        cursoSede.setCurso(cursoOpt.get());
        cursoSede.setSede(sedeOpt.get());
        cursoSede.setCursada_horario_dia(cursoSedeDTO.getCursadaHorarioDia());
        cursoSede.setCapacidad(cursoSedeDTO.getCapacidad());
        cursoSede.setVacantesDisponibles(cursoSedeDTO.getCapacidad());
        cursoSede.setHayDescuento(cursoSedeDTO.isHayDescuento());
        cursoSede.setDescuento(cursoSedeDTO.getDescuento());

        try {
            CursoSede cursoSedeGuardado = cursoSedeDAO.guardarCursoSede(cursoSede);
            // Convertir a DTO de respuesta para evitar problemas de lazy loading
            CursoSedeResponseDTO responseDTO = new CursoSedeResponseDTO(cursoSedeGuardado);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el CursoSede: " + e.getMessage());
        }
    }

    @GetMapping("/cursos/{id}")
    @Transactional
    public ResponseEntity<Curso> getCursoPorId(@PathVariable Long id) {
        Optional<Curso> cursoOpt = cursoDAO.getCursoById(id);
        if (cursoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cursoOpt.get());
    }

    @GetMapping("/cursos/{id}/con-horarios")
    @Transactional
    public ResponseEntity<Map<String, Object>> getCursoConHorarios(@PathVariable Long id, @RequestParam(required = false) Long alumnoId) {
        Optional<Curso> cursoOpt = cursoDAO.getCursoById(id);
        if (cursoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Curso curso = cursoOpt.get();
        
        // Obtener los CursoSede asociados a este curso para obtener los horarios
        List<CursoSede> cursoSedes = cursoSedeDAO.getByCursoId(id);
        
        // Crear un mapa con la información del curso y los horarios de las sedes
        Map<String, Object> cursoConHorarios = new HashMap<>();
        cursoConHorarios.put("id", curso.getId());
        cursoConHorarios.put("nombre", curso.getNombre());
        cursoConHorarios.put("fotosUrl", curso.getFotosUrl());
        cursoConHorarios.put("modalidad", curso.getModalidad());
        cursoConHorarios.put("importe", curso.getImporte());
        cursoConHorarios.put("duracion", curso.getDuracion());
        cursoConHorarios.put("objetivo", curso.getObjetivo());
        cursoConHorarios.put("fechaInicio", curso.getFechaInicio());
        cursoConHorarios.put("fechaFin", curso.getFecha_fin());
        cursoConHorarios.put("estadoCurso", curso.getEstadoCurso());
        cursoConHorarios.put("descripcion", curso.getDescripcion());
        cursoConHorarios.put("temario", curso.getTemario());
        cursoConHorarios.put("requisitos", curso.getRequisitos());
        cursoConHorarios.put("practicas", curso.getPracticas());
        cursoConHorarios.put("sedes", curso.getSedes());
        cursoConHorarios.put("inscriptos", curso.getInscriptos());
        cursoConHorarios.put("materiales", curso.getMateriales());
        
        // Agregar información de horarios desde CursoSede
        List<Map<String, Object>> sedesConHorarios = cursoSedes.stream().map(cs -> {
            Map<String, Object> sedeInfo = new HashMap<>();
            sedeInfo.put("sedeId", cs.getSede().getId());
            sedeInfo.put("sedeNombre", cs.getSede().getNombre());
            sedeInfo.put("cursadaHorarioDia", cs.getCursada_horario_dia());
            sedeInfo.put("capacidad", cs.getCapacidad());
            sedeInfo.put("vacantesDisponibles", cs.getVacantesDisponibles());
            sedeInfo.put("descuento", cs.getDescuento());
            return sedeInfo;
        }).collect(Collectors.toList());
        cursoConHorarios.put("sedesConHorarios", sedesConHorarios);
        
        // Si se proporciona alumnoId, verificar si está inscripto y obtener información de la sede
        if (alumnoId != null) {
            Optional<Alumno> alumnoOpt = alumnoDAO.getAlumnoById(alumnoId);
            if (alumnoOpt.isPresent()) {
                Alumno alumno = alumnoOpt.get();
                boolean estaInscripto = alumno.getCursosInscriptos() != null && 
                    alumno.getCursosInscriptos().stream().anyMatch(c -> c.getId().equals(id));
                
                cursoConHorarios.put("estaInscripto", estaInscripto);
                
                if (estaInscripto) {
                    // Buscar la información específica de la sede donde está inscripto
                    // Usar la misma lógica que el endpoint de cursos inscriptos
                    List<CursoSede> cursoSedesDelAlumno = cursoSedeDAO.getByCursoId(id);
                    if (!cursoSedesDelAlumno.isEmpty()) {
                        // Por ahora, tomar la primera sede disponible
                        // En una implementación real, deberías tener una relación directa entre Alumno y CursoSede
                        CursoSede cursoSede = cursoSedesDelAlumno.get(0);
                        cursoConHorarios.put("sedeInscripto", cursoSede.getSede().getNombre());
                        cursoConHorarios.put("horarioInscripto", cursoSede.getCursada_horario_dia());
                    }
                }
            }
        }
        
        return ResponseEntity.ok(cursoConHorarios);
    }

    @GetMapping("/{id}/cuenta-corriente")
    public ResponseEntity<?> obtenerCuentaCorriente(@PathVariable Long id) {
        // Buscar cuenta corriente por id de alumno
        Optional<CuentaCorriente> cuentaOpt = cuentaCorrienteDAO.getCuentaCorrienteByAlumnoId(id);
        if (cuentaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró cuenta corriente para el alumno");
        }
        CuentaCorriente cuenta = cuentaOpt.get();
        List<Pago> facturas = pagoDAO.getPagosPorCuentaCorriente(cuenta.getId());
        
        // Calcular el total basado en las facturas obtenidas
        cuenta.recalcularTotalConFacturas(facturas);
        
        // Separar por estado
        List<Pago> aPagar = facturas.stream().filter(p -> p.getEstado() == Pago.EstadoPago.A_PAGAR).toList();
        List<Pago> pagas = facturas.stream().filter(p -> p.getEstado() == Pago.EstadoPago.PAGO).toList();
        List<Pago> reembolsados = facturas.stream().filter(p -> p.getEstado() == Pago.EstadoPago.REEMBOLSADO).toList();
        
        // Convertir a mapas simples para evitar problemas de lazy loading
        List<Map<String, Object>> aPagarMap = aPagar.stream().map(this::convertirPagoAMap).toList();
        List<Map<String, Object>> pagasMap = pagas.stream().map(this::convertirPagoAMap).toList();
        List<Map<String, Object>> reembolsadosMap = reembolsados.stream().map(this::convertirPagoAMap).toList();
        
        // Armar respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("cuentaCorrienteId", cuenta.getId()); // <-- AGREGA ESTA LÍNEA
        response.put("total", cuenta.getTotalFacturas());
        response.put("saldoFavor", cuenta.getSaldoFavor() != null ? cuenta.getSaldoFavor() : 0f);
        response.put("aPagar", aPagarMap);
        response.put("pagas", pagasMap);
        response.put("reembolsados", reembolsadosMap);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> convertirPagoAMap(Pago pago) {
        Map<String, Object> pagoMap = new HashMap<>();
        pagoMap.put("id", pago.getId());
        pagoMap.put("importe", pago.getImporte());
        pagoMap.put("estado", pago.getEstado().toString());
        pagoMap.put("fechaInicioCurso", pago.getFechaInicioCurso() != null ? pago.getFechaInicioCurso().toString() : null);
        
        // Solo datos básicos del curso
        if (pago.getCurso() != null) {
            Map<String, Object> cursoData = new HashMap<>();
            cursoData.put("id", pago.getCurso().getId());
            cursoData.put("nombre", pago.getCurso().getNombre());
            pagoMap.put("curso", cursoData);
        }
        
        // Solo datos básicos de la sede
        if (pago.getSede() != null) {
            Map<String, Object> sedeData = new HashMap<>();
            sedeData.put("id", pago.getSede().getId());
            sedeData.put("nombre", pago.getSede().getNombre());
            pagoMap.put("sede", sedeData);
        }
        
        return pagoMap;
    }


    @PostMapping("/pagos/crear")
    @Transactional
    public ResponseEntity<?> crearPago(@RequestBody Pago pago) {
        // Validar datos obligatorios
        if (pago.getCuentaCorriente() == null || pago.getCuentaCorriente().getId() == null) {
            return ResponseEntity.badRequest().body("La cuenta corriente es obligatoria.");
        }
        
        if (pago.getCurso() == null || pago.getCurso().getId() == null) {
            return ResponseEntity.badRequest().body("El curso es obligatorio.");
        }
        
        if (pago.getImporte() <= 0) {
            return ResponseEntity.badRequest().body("El importe debe ser mayor a 0.");
        }

        // Verificar que la cuenta corriente existe
        Optional<CuentaCorriente> cuentaOpt = cuentaCorrienteDAO.getCuentaCorrienteById(pago.getCuentaCorriente().getId());
        if (cuentaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cuenta corriente no encontrada.");
        }

        // Verificar que el curso existe
        Optional<Curso> cursoOpt = cursoDAO.getCursoById(pago.getCurso().getId());
        if (cursoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Curso no encontrado.");
        }

        // Asignar las entidades completas
        pago.setCuentaCorriente(cuentaOpt.get());
        pago.setCurso(cursoOpt.get());
        
        // Si no se especifica estado, poner por defecto A_PAGAR
        if (pago.getEstado() == null) {
            pago.setEstado(Pago.EstadoPago.A_PAGAR);
        }

        // Guardar el pago
        Pago pagoGuardado = pagoDAO.guardarPago(pago);

        // Recalcular total de la cuenta corriente
        CuentaCorriente cuenta = cuentaOpt.get();
        // Obtener las facturas actualizadas de la base de datos
        List<Pago> facturas = pagoDAO.getPagosPorCuentaCorriente(cuenta.getId());
        cuenta.recalcularTotalConFacturas(facturas);
        cuentaCorrienteDAO.actualizarCuentaCorriente(cuenta);

        // Crear respuesta simplificada con solo los datos necesarios
        Map<String, Object> response = new HashMap<>();
        response.put("id", pagoGuardado.getId());
        response.put("importe", pagoGuardado.getImporte());
        response.put("estado", pagoGuardado.getEstado());
        response.put("fechaInicioCurso", pagoGuardado.getFechaInicioCurso() != null ? pagoGuardado.getFechaInicioCurso().toString() : null);
        
        // Solo datos básicos del curso
        Map<String, Object> cursoData = new HashMap<>();
        cursoData.put("id", pagoGuardado.getCurso().getId());
        cursoData.put("nombre", pagoGuardado.getCurso().getNombre());
        response.put("curso", cursoData);
        
        // Solo ID de la cuenta corriente
        response.put("cuentaCorrienteId", pagoGuardado.getCuentaCorriente().getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/curso-sedes")
    public ResponseEntity<List<CursoSede>> obtenerCursoSedesCompletos(@PathVariable Long id) {
        List<CursoSede> cursoSedes = cursoSedeDAO.getByCursoId(id);
        return ResponseEntity.ok(cursoSedes);
    }

    @GetMapping("/alumno/{id}")
    public ResponseEntity<?> obtenerAlumnoPorId(@PathVariable Long id) {
        Optional<Alumno> alumnoOpt = alumnoDAO.getAlumnoById(id);
        if (alumnoOpt.isPresent()) {
            return ResponseEntity.ok(alumnoOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alumno no encontrado");
        }
    }

    @PostMapping("/inscribir-alumno-curso")
    @Transactional
    public ResponseEntity<?> inscribirAlumnoEnCurso(@RequestBody InscripcionRequest request) {
        // Validar datos de entrada
        if (request.getAlumnoId() == null || request.getCursoId() == null || request.getCursoSedeId() == null) {
            return ResponseEntity.badRequest().body("Los IDs de alumno, curso y cursoSede son obligatorios.");
        }

        try {
            // Buscar el alumno
            Optional<Alumno> alumnoOpt = alumnoDAO.getAlumnoById(request.getAlumnoId());
            if (alumnoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alumno no encontrado con ID: " + request.getAlumnoId());
            }

            // Buscar el curso
            Optional<Curso> cursoOpt = cursoDAO.getCursoById(request.getCursoId());
            if (cursoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Curso no encontrado con ID: " + request.getCursoId());
            }

            // Buscar el CursoSede
            Optional<CursoSede> cursoSedeOpt = cursoSedeDAO.getCursoSedeById(request.getCursoSedeId());
            if (cursoSedeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CursoSede no encontrado con ID: " + request.getCursoSedeId());
            }

            Alumno alumno = alumnoOpt.get();
            Curso curso = cursoOpt.get();
            CursoSede cursoSede = cursoSedeOpt.get();

            // Verificar que el CursoSede corresponda al curso
            if (!cursoSede.getCurso().getId().equals(curso.getId())) {
                return ResponseEntity.badRequest().body("El CursoSede no corresponde al curso especificado.");
            }

            // Verificar que hay vacantes disponibles
            if (cursoSede.getVacantesDisponibles() <= 0) {
                return ResponseEntity.badRequest().body("No hay vacantes disponibles para este curso en la sede seleccionada.");
            }

            // Verificar que el alumno no esté ya inscripto en este curso (sin importar la sede)
            if (alumno.getCursosInscriptos() != null &&
                    alumno.getCursosInscriptos().stream().anyMatch(c -> c.getId().equals(curso.getId()))) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Ya estás inscripto en este curso: " + curso.getNombre() + ". Solo puedes inscribirte una vez por curso.");
            }

            // Inicializar listas si son null
            if (alumno.getCursosInscriptos() == null) {
                alumno.setCursosInscriptos(new ArrayList<>());
            }
            if (curso.getInscriptos() == null) {
                curso.setInscriptos(new ArrayList<>());
            }

            // Agregar el curso al alumno
            alumno.getCursosInscriptos().add(curso);

            // Agregar el alumno al curso
            curso.getInscriptos().add(alumno);

            // Actualizar vacantes disponibles en CursoSede
            cursoSede.actualizarVacantesDisponibles();

            // Guardar todos los cambios
            alumnoDAO.actualizarAlumno(alumno);
            cursoDAO.actualizarCurso(curso);
            cursoSedeDAO.actualizarCursoSede(cursoSede);

            // Definir en qué clase estará la fecha de hoy
            int claseHoy = 3;

            // Fecha hoy
            LocalDateTime fechaHoy = LocalDateTime.now();

            // Calcular fecha base para la clase 1 (restar semanas a fechaHoy)
            LocalDateTime fechaClase1 = fechaHoy.minusWeeks(claseHoy - 1);

            // Crear asistencias con fechas ordenadas (clase 1 a clase 14)
            for (int i = 1; i <= 14; i++) {
                Asistencia asistencia = new Asistencia();
                asistencia.setAlumno(alumno);
                asistencia.setCurso(curso);
                asistencia.setClase(i);
                // Fecha de la clase i: suma (i-1) semanas a la fechaClase1
                asistencia.setFecha(fechaClase1.plusWeeks(i - 1));
                asistencia.setAsistencia(null);

                asistenciaDAO.agregarAsistencia(asistencia);
            }

            // Resto igual...
            Optional<CuentaCorriente> cuentaOpt = cuentaCorrienteDAO.getCuentaCorrienteByAlumnoId(alumno.getId());
            if (cuentaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: No se encontró la cuenta corriente del alumno.");
            }

            CuentaCorriente cuentaCorriente = cuentaOpt.get();

            float importeFinal = curso.getImporte();
            if (cursoSede.isHayDescuento() && cursoSede.getDescuento() > 0) {
                float descuentoAplicado = curso.getImporte() * (cursoSede.getDescuento() / 100.0f);
                importeFinal = curso.getImporte() - descuentoAplicado;
            }

            // Aplicar saldo a favor si existe
            Float saldoFavor = cuentaCorriente.getSaldoFavor() != null ? cuentaCorriente.getSaldoFavor() : 0f;
            boolean cubiertoPorSaldo = false;
            if (saldoFavor > 0) {
                if (saldoFavor >= importeFinal) {
                    // El saldo cubre todo el curso
                    cuentaCorriente.setSaldoFavor(saldoFavor - importeFinal);
                    importeFinal = 0f;
                    cubiertoPorSaldo = true;
                } else {
                    // El saldo cubre parte del curso
                    importeFinal = importeFinal - saldoFavor;
                    cuentaCorriente.setSaldoFavor(0f);
                }
                cuentaCorrienteDAO.actualizarCuentaCorriente(cuentaCorriente);
            }

            Pago pago = new Pago();
            pago.setCuentaCorriente(cuentaCorriente);
            pago.setCurso(curso);
            pago.setSede(cursoSede.getSede());
            pago.setImporte(importeFinal);
            pago.setEstado(cubiertoPorSaldo ? Pago.EstadoPago.PAGO : Pago.EstadoPago.A_PAGAR);

            LocalDateTime fechaInicioCurso = curso.getFechaInicio();
            pago.setFechaInicioCurso(fechaInicioCurso);

            Pago pagoGuardado = pagoDAO.guardarPago(pago);

            List<Pago> facturas = pagoDAO.getPagosPorCuentaCorriente(cuentaCorriente.getId());
            cuentaCorriente.recalcularTotalConFacturas(facturas);
            cuentaCorrienteDAO.actualizarCuentaCorriente(cuentaCorriente);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Inscripción realizada exitosamente");
            response.put("alumnoId", alumno.getId());
            response.put("cursoId", curso.getId());
            response.put("cursoSedeId", cursoSede.getId());
            response.put("vacantesDisponibles", cursoSede.getVacantesDisponibles());
            response.put("pagoCreado", true);
            response.put("pagoId", pagoGuardado.getId());
            response.put("importeOriginal", curso.getImporte());
            response.put("importeFinal", pagoGuardado.getImporte());
            response.put("descuentoAplicado", cursoSede.isHayDescuento() ? cursoSede.getDescuento() : 0);
            response.put("fechaInicioCurso", pagoGuardado.getFechaInicioCurso() != null ? pagoGuardado.getFechaInicioCurso().toString() : null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al realizar la inscripción: " + e.getMessage());
        }
    }


    @GetMapping("/cursos/{cursoId}/inscriptos")
    public ResponseEntity<?> obtenerInscriptosPorCurso(@PathVariable Long cursoId) {
        try {
            // Buscar el curso
            Optional<Curso> cursoOpt = cursoDAO.getCursoById(cursoId);
            if (cursoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Curso no encontrado con ID: " + cursoId);
            }

            Curso curso = cursoOpt.get();
            List<Alumno> inscriptos = curso.getInscriptos();

            if (inscriptos == null || inscriptos.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            // Convertir a DTOs para evitar problemas de serialización
            List<Map<String, Object>> inscriptosDTO = inscriptos.stream().map(alumno -> {
                Map<String, Object> alumnoMap = new HashMap<>();
                alumnoMap.put("id", alumno.getId());
                alumnoMap.put("nombreCompleto", alumno.getNombreCompleto());
                
                // Datos del usuario asociado
                if (alumno.getUsuario() != null) {
                    Map<String, Object> usuarioMap = new HashMap<>();
                    usuarioMap.put("id", alumno.getUsuario().getId());
                    usuarioMap.put("email", alumno.getUsuario().getEmail());
                    usuarioMap.put("alias", alumno.getUsuario().getAlias());
                    usuarioMap.put("nombre", alumno.getUsuario().getNombre());
                    usuarioMap.put("apellido", alumno.getUsuario().getApellido());
                    alumnoMap.put("usuario", usuarioMap);
                }
                
                return alumnoMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(inscriptosDTO);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener los inscriptos: " + e.getMessage());
        }
    }

    @PostMapping("/baja-curso")
    @Transactional
    public ResponseEntity<?> darBajaDeCurso(@RequestBody BajaCursoRequest request) {
        try {
            // Validar datos de entrada
            if (request.getAlumnoId() == null || request.getCursoId() == null || 
                request.getTipoReintegro() == null || request.getMontoReintegro() < 0) {
                return ResponseEntity.badRequest().body("Datos de entrada inválidos");
            }

            // Buscar el alumno
            Optional<Alumno> alumnoOpt = alumnoDAO.getAlumnoById(request.getAlumnoId());
            if (alumnoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alumno no encontrado con ID: " + request.getAlumnoId());
            }

            // Buscar el curso
            Optional<Curso> cursoOpt = cursoDAO.getCursoById(request.getCursoId());
            if (cursoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Curso no encontrado con ID: " + request.getCursoId());
            }

            Alumno alumno = alumnoOpt.get();
            Curso curso = cursoOpt.get();

            // Verificar que el alumno esté inscripto en este curso
            if (alumno.getCursosInscriptos() == null || 
                !alumno.getCursosInscriptos().stream().anyMatch(c -> c.getId().equals(curso.getId()))) {
                return ResponseEntity.badRequest().body("El alumno no está inscripto en este curso.");
            }

            // Procesar el reintegro según el tipo
            if ("SALDO_FAVOR".equals(request.getTipoReintegro()) && request.getMontoReintegro() > 0) {
                // Agregar saldo a favor a la cuenta corriente
                Optional<CuentaCorriente> cuentaCorrienteOpt = cuentaCorrienteDAO.getCuentaCorrienteByAlumnoId(alumno.getId());
                CuentaCorriente cuentaCorriente;
                
                if (cuentaCorrienteOpt.isEmpty()) {
                    // Crear cuenta corriente si no existe
                    cuentaCorriente = new CuentaCorriente();
                    cuentaCorriente.setAlumno(alumno);
                    cuentaCorriente.setTotalFacturas(0f);
                    cuentaCorriente.setSaldoFavor(0f);
                    cuentaCorrienteDAO.guardarCuentaCorriente(cuentaCorriente);
                } else {
                    cuentaCorriente = cuentaCorrienteOpt.get();
                }
                
                // Agregar el saldo a favor
                Float saldoActual = cuentaCorriente.getSaldoFavor();
                if (saldoActual == null) {
                    saldoActual = 0f;
                }
                cuentaCorriente.setSaldoFavor(saldoActual + request.getMontoReintegro());
                cuentaCorrienteDAO.actualizarCuentaCorriente(cuentaCorriente);
            }
            // Si es TARJETA, no hacemos nada aquí (se procesaría externamente)

            // Remover el curso del alumno
            alumno.getCursosInscriptos().removeIf(c -> c.getId().equals(curso.getId()));

            // Remover el alumno del curso
            if (curso.getInscriptos() != null) {
                curso.getInscriptos().removeIf(a -> a.getId().equals(alumno.getId()));
            }

            // Actualizar vacantes disponibles en todos los CursoSede de este curso
            List<CursoSede> cursoSedes = cursoSedeDAO.getByCursoId(request.getCursoId());
            for (CursoSede cursoSede : cursoSedes) {
                cursoSede.actualizarVacantesDisponibles();
                cursoSedeDAO.actualizarCursoSede(cursoSede);
            }

            // Guardar los cambios
            alumnoDAO.actualizarAlumno(alumno);
            cursoDAO.actualizarCurso(curso);

            // Crear respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Baja realizada exitosamente del curso: " + curso.getNombre());
            response.put("alumnoId", alumno.getId());
            response.put("cursoId", curso.getId());
            response.put("tipoReintegro", request.getTipoReintegro());
            response.put("montoReintegro", request.getMontoReintegro());
            response.put("porcentajeReintegro", request.getPorcentajeReintegro());

            // Eliminar asistencias del alumno para ese curso
            List<Asistencia> asistencias = asistenciaDAO.getAsistenciasPorAlumnoYCurso(alumno.getId(), curso.getId());
            for (Asistencia asistencia : asistencias) {
                asistenciaDAO.eliminarAsistencia(asistencia.getId());
            }

            // Cambiar estado de pagos asociados a este curso y cuenta corriente a REEMBOLSADO si están en estado A_PAGAR
            if (request.getCuentaCorrienteId() != null) {
                List<Pago> pagos = pagoDAO.getPagosPorCuentaCorriente(request.getCuentaCorrienteId());
                List<Pago> pagosCurso = pagos.stream()
                    .filter(p -> p.getCurso() != null
                        && p.getCurso().getId().equals(curso.getId())
                        && p.getEstado() == Pago.EstadoPago.A_PAGAR)
                    .collect(Collectors.toList());

                for (Pago pago : pagosCurso) {
                    pago.setEstado(Pago.EstadoPago.REEMBOLSADO);
                    pagoDAO.actualizarPago(pago);
                }
                // Recalcular el total de la cuenta corriente (solo con pagos A_PAGAR)
                List<Pago> pagosRestantes = pagoDAO.getPagosPorCuentaCorriente(request.getCuentaCorrienteId());
                CuentaCorriente cuentaCorriente = cuentaCorrienteDAO.getCuentaCorrienteById(request.getCuentaCorrienteId()).orElse(null);
                if (cuentaCorriente != null) {
                    List<Pago> pagosAPagar = pagosRestantes.stream().filter(p -> p.getEstado() == Pago.EstadoPago.A_PAGAR).toList();
                    if (pagosAPagar.isEmpty()) {
                        cuentaCorriente.setTotalFacturas(0f);
                    } else {
                        cuentaCorriente.recalcularTotalConFacturas(pagosAPagar);
                    }
                    cuentaCorrienteDAO.actualizarCuentaCorriente(cuentaCorriente);
                }
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al realizar la baja: " + e.getMessage());
        }
    }

    @GetMapping("/sedes/{id}/cursos")
    public ResponseEntity<List<Map<String, Object>>> getCursosPorSede(@PathVariable Long id) {
        List<CursoSede> cursosSede = cursoSedeDAO.getBySedeId(id);

        if (cursosSede.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> cursosDTO = new ArrayList<>();
        for (CursoSede cs : cursosSede) {
            // Actualizar las vacantes disponibles antes de devolver los datos
            cs.actualizarVacantesDisponibles();
            
            Map<String, Object> cursoMap = new HashMap<>();
            cursoMap.put("id", cs.getCurso().getId());
            cursoMap.put("nombre", cs.getCurso().getNombre());
            cursoMap.put("descripcion", cs.getCurso().getDescripcion());
            cursoMap.put("capacidad", cs.getCapacidad());
            cursoMap.put("vacantesDisponibles", cs.getVacantesDisponibles());
            cursoMap.put("horario", cs.getCursada_horario_dia());
            cursoMap.put("precio", cs.getCurso().getImporte());

            // Ahora el descuento es de CursoSede
            cursoMap.put("descuento", cs.getDescuento());

            cursosDTO.add(cursoMap);
        }

        return ResponseEntity.ok(cursosDTO);
    }


    @GetMapping("/sedes/tarjetas")
    public ResponseEntity<List<Map<String, Object>>> obtenerSedesParaTarjetas() {
        List<Sede> sedes = sedeDAO.getAllSedes();

        List<Map<String, Object>> tarjetas = new ArrayList<>();

        for (Sede sede : sedes) {
            // Obtener la cantidad de cursos disponibles en esta sede
            List<CursoSede> cursosSede = cursoSedeDAO.getBySedeId(sede.getId());
            int cantidadCursos = cursosSede.size();
            
            Map<String, Object> tarjeta = new HashMap<>();
            tarjeta.put("id", sede.getId());
            tarjeta.put("nombre", sede.getNombre());
            tarjeta.put("direccion", sede.getDireccion());
            tarjeta.put("telefono", sede.getTelefono());
            tarjeta.put("fotoPrincipal", sede.getFotosUrlSedes().isEmpty() ? null : sede.getFotosUrlSedes().get(0));
            tarjeta.put("zona", sede.getZona());
            tarjeta.put("cantidadCursos", cantidadCursos);
            tarjetas.add(tarjeta);
        }

        return ResponseEntity.ok(tarjetas);
    }

    @GetMapping("/{alumnoId}/cursos-inscriptos")
    public ResponseEntity<?> obtenerCursosInscriptosPorAlumno(@PathVariable Long alumnoId) {
        try {
            Optional<Alumno> alumnoOpt = alumnoDAO.getAlumnoById(alumnoId);
            if (alumnoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alumno no encontrado con ID: " + alumnoId);
            }

            Alumno alumno = alumnoOpt.get();
            List<Curso> cursosInscriptos = alumno.getCursosInscriptos();

            if (cursosInscriptos == null || cursosInscriptos.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            List<Map<String, Object>> cursosDTO = new ArrayList<>();

            for (Curso curso : cursosInscriptos) {
                // Obtener todas las sedes de ese curso
                List<CursoSede> cursoSedes = cursoSedeDAO.getByCursoId(curso.getId());

                // Suponemos que hay una sola sede en la que se inscribió este alumno,
                // y usamos la primera (por ahora)
                if (!cursoSedes.isEmpty()) {
                    CursoSede cursoSede = cursoSedes.get(0); // <-- esta parte depende de la lógica real

                    Map<String, Object> cursoMap = new HashMap<>();
                    cursoMap.put("cursoId", curso.getId());
                    cursoMap.put("nombre", curso.getNombre());
                    cursoMap.put("descripcion", curso.getDescripcion());
                    cursoMap.put("imagenUrl", curso.getFotosUrl().isEmpty() ? null : curso.getFotosUrl().get(0));
                    cursoMap.put("modalidad", curso.getModalidad());
                    cursoMap.put("estadoCurso", curso.getEstadoCurso());
                    cursoMap.put("horarioCursada", cursoSede.getCursada_horario_dia());
                    cursoMap.put("sedeId", cursoSede.getSede().getId());
                    cursoMap.put("sedeNombre", cursoSede.getSede().getNombre());
                    cursoMap.put("cursoSedeId", cursoSede.getId());

                    cursosDTO.add(cursoMap);
                }
            }

            return ResponseEntity.ok(cursosDTO);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener los cursos inscriptos: " + e.getMessage());
        }
    }

    @GetMapping("/{alumnoId}/cursos/{cursoId}/asistencias")
    public ResponseEntity<?> getAsistenciasPorAlumnoYCurso(@PathVariable Long alumnoId, @PathVariable Long cursoId) {
        try {
            List<Asistencia> asistencias = asistenciaDAO.getAsistenciasPorAlumnoYCurso(alumnoId, cursoId);
            return ResponseEntity.ok(asistencias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener asistencias: " + e.getMessage());
        }
    }



    // Obtener materiales de clase por curso
    @GetMapping("/cursos/{cursoId}/materiales")
    public ResponseEntity<List<MaterialDeClase>> getMaterialesPorCurso(@PathVariable Long cursoId) {
        List<MaterialDeClase> materiales = materialDeClaseDAO.getMaterialesPorCurso(cursoId);
        return ResponseEntity.ok(materiales);
    }

    // Descargar material de clase
    @GetMapping("/materiales/{materialId}/descargar")
    public ResponseEntity<Resource> descargarMaterial(@PathVariable Long materialId) {
        Optional<MaterialDeClase> materialOpt = materialDeClaseDAO.getMaterialById(materialId);
        if (materialOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        MaterialDeClase material = materialOpt.get();
        try {
            Resource resource = new UrlResource(material.getArchivoUrl());
            String contentType = "application/octet-stream";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + material.getNombre() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/asistencias/marcarPresenteConFecha/{alumnoId}/{cursoId}/{fecha}")
    @Transactional
    public ResponseEntity<?> marcarPresenteConFecha(
            @PathVariable Long alumnoId,
            @PathVariable Long cursoId,
            @PathVariable String fecha) {
        try {
            LocalDate fechaClase;

            try {
                // Primero intenta parsear como fecha completa con hora
                if (fecha.contains("T")) {
                    fechaClase = LocalDateTime.parse(fecha, DateTimeFormatter.ISO_DATE_TIME).toLocalDate();
                } else {
                    // Si no tiene "T", parsea como solo fecha
                    fechaClase = LocalDate.parse(fecha, DateTimeFormatter.ISO_DATE);
                }
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Formato de fecha inválido. Use 'yyyy-MM-dd' o ISO_DATE_TIME.");
            }

            List<Asistencia> asistencias = asistenciaDAO.getAsistenciasPorAlumnoYCurso(alumnoId, cursoId);

            Asistencia asistenciaClase = asistencias.stream()
                    .filter(a -> a.getFecha() != null && a.getFecha().toLocalDate().isEqual(fechaClase))
                    .findFirst()
                    .orElse(null);

            if (asistenciaClase == null) {
                return ResponseEntity.badRequest().body("No hay asistencia registrada para la fecha proporcionada.");
            }

            if ("Presente".equalsIgnoreCase(asistenciaClase.getAsistencia())) {
                return ResponseEntity.badRequest().body("La asistencia ya está marcada como presente para esta fecha.");
            }

            asistenciaClase.setAsistencia("Presente");
            asistenciaClase.setFecha(LocalDateTime.now());
            asistenciaDAO.actualizarAsistencia(asistenciaClase);

            return ResponseEntity.ok(asistenciaClase);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/marcarAusenteConFecha/asistencias/{alumnoId}/{cursoId}/{fecha}")
    @Transactional
    public ResponseEntity<?> marcarAusenteConFecha(@PathVariable Long alumnoId,
                                                   @PathVariable Long cursoId,
                                                   @PathVariable String fecha) {
        try {
            // Convertir la fecha del path a LocalDate
            LocalDate fechaObjetivo = LocalDate.parse(fecha);

            // Obtener todas las asistencias del alumno en ese curso
            List<Asistencia> asistencias = asistenciaDAO.getAsistenciasPorAlumnoYCurso(alumnoId, cursoId);

            // Buscar la asistencia cuya fecha (sin hora) coincide con la enviada
            Asistencia asistenciaObjetivo = asistencias.stream()
                    .filter(a -> {
                        if (a.getFecha() == null || a.getAsistencia() != null) return false;
                        LocalDate fechaA = a.getFecha().toLocalDate();
                        return fechaA.equals(fechaObjetivo);
                    })
                    .findFirst()
                    .orElse(null);

            if (asistenciaObjetivo == null) {
                return ResponseEntity.badRequest().body("No hay asistencia sin marcar en la fecha " + fecha);
            }

            // Marcar como ausente
            asistenciaObjetivo.setAsistencia("Ausente");
            asistenciaObjetivo.setFecha(LocalDateTime.now()); // Guardamos fecha actual del marcado
            asistenciaDAO.actualizarAsistencia(asistenciaObjetivo);

            return ResponseEntity.ok(asistenciaObjetivo);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al marcar ausente: " + e.getMessage());
        }
    }

}
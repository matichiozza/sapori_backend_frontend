package com.example.sapori.network;

import com.example.sapori.model.Alumno;
import com.example.sapori.CambioContraseniaRequest;
import com.example.sapori.model.Asistencia;
import com.example.sapori.model.ComentarioValoracion;
import com.example.sapori.model.ConfirmacionRequest;
import com.example.sapori.model.Curso;
import com.example.sapori.model.Ingrediente;
import com.example.sapori.model.IngredienteDTO;
import com.example.sapori.model.IngredienteReceta;
import com.example.sapori.model.PasoReceta;
import com.example.sapori.model.Receta;
import com.example.sapori.model.RecetaCalculadaDTO;
import com.example.sapori.model.SedeVacanteResponse;
import com.example.sapori.model.Usuario;
import com.example.sapori.model.CursoSede;
import com.example.sapori.model.InscripcionRequest;
import com.example.sapori.model.BajaCursoRequest;
import com.example.sapori.model.MaterialDeClase;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("/usuarios/registro/iniciar")
    Call<Usuario> iniciarRegistro(@Body Usuario usuario);

    @POST("/usuarios/registro/validar-codigo")
    Call<ResponseBody> validarCodigo(@Body ConfirmacionRequest request);

    @POST("/usuarios/registro/completar")
    Call<Usuario> completarRegistro(@Body Usuario usuario);

    @POST("/usuarios/verificar/alias")
    Call<Map<String, Object>> verificarAlias(@Body Map<String, String> alias);

    @POST("/usuarios/verificar/email")
    Call<Map<String, String>> verificarEmail(@Body Map<String, String> emailBody);

    @POST("/usuarios/login")
    Call<Usuario> login(@Body Map<String, String> credentials);

    // Recuperación de contraseña
    @POST("/usuarios/recuperar/enviar-codigo")
    Call<ResponseBody> enviarCodigoRecuperacion(@Body Usuario usuario);

    @POST("/usuarios/recuperar/validar-codigo")
    Call<ResponseBody> validarCodigoRecuperacion(@Body ConfirmacionRequest request);

    @POST("/usuarios/recuperar/cambiar-contrasenia")
    Call<ResponseBody> cambiarContrasenia(@Body CambioContraseniaRequest request);

    @GET("usuarios/{email}")
    Call<Usuario> obtenerUsuarioPorEmail(@Path("email") String email);

    @Multipart
    @POST("usuarios/recetas/{id}/imagen")
    Call<ResponseBody> subirImagenReceta(@Path("id") Long id, @Part MultipartBody.Part imagen);

    @POST("usuarios/recetas/{id}/ingredientes")
    Call<ResponseBody> agregarIngredientesReceta(@Path("id") Long recetaId, @Body List<IngredienteReceta> ingredientes);

    @PUT("usuarios/{email}")
    Call<Usuario> actualizarUsuario(@Path("email") String email, @Body Usuario usuario);

    @Multipart
    @POST("usuarios/{email}/foto-perfil")
    Call<ResponseBody> subirFotoPerfil(
            @Path("email") String email,
            @Part MultipartBody.Part foto
    );

    @GET("/usuarios/recetas/tarjetas")
    Call<List<Map<String, Object>>> obtenerRecetasParaTarjetas();

    @GET("/usuarios/cursos/tarjetas")
    Call<List<Map<String, Object>>> obtenerCursosParaTarjetas();

    @POST("recetas/filtrar")
    Call<List<Map<String, Object>>> obtenerRecetasPorTipos(@Body List<String> tipos);


    @GET("/usuarios/recetas/buscar/{nombre}/{alias}")
    Call<List<Receta>> buscarRecetasPorNombreYAlias(@Path("nombre") String nombre, @Path("alias") String alias);

    @GET("/usuarios/recetas/porUsuario/{alias}")
    Call<List<Map<String, Object>>> obtenerRecetasPorAlias(@Path("alias") String alias);

    @GET("/usuarios/recetas/ordenadas/alfabeticamente")
    Call<List<Map<String, Object>>> obtenerRecetasOrdenadasAlfabeticamente();

    @GET("/usuarios/recetas/ordenadas/porFecha")
    Call<List<Map<String, Object>>> obtenerRecetasOrdenadasPorFechaDesc();
    @GET("/usuarios/recetas/ordenadas/porFechaAsc")
    Call<List<Map<String, Object>>> obtenerRecetasOrdenadasPorFechaAsc();

    @GET("/usuarios/listado")
    Call<List<Ingrediente>> listarIngredientes();

    @POST("/usuarios/{id}/favoritos/{recetaId}")
    Call<Void> agregarRecetaAFavoritos(
            @Path("id") Long id,
            @Path("recetaId") Long recetaId
    );

    @DELETE("/usuarios/{id}/favoritos/{recetaId}")
    Call<Void> eliminarRecetaDeFavoritos(
            @Path("id") Long id,
            @Path("recetaId") Long recetaId
    );

    @GET("/usuarios/{id}/favoritos")
    Call<List<Receta>> obtenerRecetasFavoritasPorId(
            @Path("id") Long id
    );



    @GET("/usuarios/recetas/{id}")
    Call<Receta> obtenerReceta(@Path("id") Long id);



    @GET("/usuarios/{id}/recetas-calculadas")
    Call<List<Receta>> obtenerRecetasCalculadasPorUsuario(@Path("id") Long id);

    @GET("/usuarios/recetas-calculadas1/{id}")
    Call<RecetaCalculadaDTO> obtenerRecetaCalculadaPorId(@Path("id") Long id);


    @POST("/usuarios/recetas/{id}/comentarios")
    Call<ComentarioValoracion> enviarComentario(@Path("id") Long recetaId, @Body ComentarioValoracion comentario);

    @POST("/usuarios/recetas/{id}/guardar-escalada")
    Call<Void> guardarRecetaEscalada(
            @Path("id") long recetaId,
            @Body RequestBody datos
    );

    @DELETE("/usuarios/recetas-calculadas/{id}")
    Call<Void> eliminarRecetaEscalada(
            @Path("id") long recetaCalculadaId
    );

    @DELETE("/usuarios/recetas/eliminar/{alias}/{idReceta}")
    Call<Void> eliminarRecetaPorAlias(
            @Path("alias") String alias,
            @Path("idReceta") Long idReceta
    );

    @GET("/usuarios/recetas-calculadas/{usuarioId}")
    Call<List<RecetaCalculadaDTO>> getRecetasCalculadas(@Path("usuarioId") Long usuarioId);


    @GET("/usuarios/ingredientes-json/receta/{recetaId}")
    Call<List<IngredienteDTO>> getIngredientesJsonPorReceta(@Path("recetaId") Long recetaId);
    @POST("/usuarios/crearReceta/{alias}")
    Call<Receta> crearReceta(@Path("alias") String alias, @Body Receta receta);
    @PUT("/usuarios/actualizarReceta/{id}")
    Call<Receta> actualizarReceta(@Path("id") Long id, @Body Receta receta);

    @POST("usuarios/recetas/{id}/pasos")
    Call<ResponseBody> agregarPasosReceta(@Path("id") Long recetaId, @Body List<PasoReceta> pasos);

    @POST("/usuarios/crearAlumno1")
    Call<Alumno> crearAlumno1(@Body Alumno alumno);

    @Multipart
    @POST("/usuarios/actualizarAlumnoDatosFinales")
    Call<ResponseBody> finalizarRegistroAlumno(
            @Part("idUsuario") RequestBody idUsuario,
            @Part("numeroTramite") RequestBody numeroTramite,
            @Part MultipartBody.Part imagenDniFrente,
            @Part MultipartBody.Part imagenDniDorso
    );

    @GET("/usuarios/usuarios/{id}")
    Call<Usuario> obtenerUsuarioPorId(@Path("id") long id);

    @GET("/usuarios/cursos/{id}")
    Call<Curso> obtenerCurso(@Path("id") Long id);

    @GET("/usuarios/cursos/{id}/con-horarios")
    Call<Map<String, Object>> obtenerCursoConHorarios(@Path("id") Long id, @Query("alumnoId") Long alumnoId);

    @GET("/usuarios/{id}/sedes")
    Call<List<SedeVacanteResponse>> obtenerSedesPorCurso(@Path("id") Long cursoId);

    @GET("/usuarios/{id}/cuenta-corriente")
    Call<Map<String, Object>> obtenerCuentaCorriente(@Path("id") Long usuarioId);

    @GET("/usuarios/{id}/sedes")
    Call<List<CursoSede>> obtenerCursoSedesPorCurso(@Path("id") Long cursoId);

    @GET("/usuarios/{id}/curso-sedes")
    Call<List<CursoSede>> obtenerCursoSedesCompletos(@Path("id") Long cursoId);

    @GET("/usuarios/alumno/{id}")
    Call<Alumno> obtenerAlumnoPorId(@Path("id") Long id);

    @POST("/usuarios/inscribir-alumno-curso")
    Call<Map<String, Object>> inscribirAlumnoEnCurso(@Body InscripcionRequest request);

    @GET("/usuarios/cursos/{cursoId}/inscriptos")
    Call<List<Map<String, Object>>> obtenerInscriptosPorCurso(@Path("cursoId") Long cursoId);

    @DELETE("/usuarios/alumno/{alumnoId}/curso/{cursoId}/baja")
    Call<Map<String, Object>> darBajaDeCurso(@Path("alumnoId") Long alumnoId, @Path("cursoId") Long cursoId);

    @GET("/usuarios/sedes/{id}/cursos")
    Call<List<Map<String, Object>>> getCursosPorSede(@Path("id") Long sedeId);
    @GET("/usuarios/sedes/tarjetas")
    Call<List<Map<String, Object>>> obtenerSedesParaTarjetas();

    @GET("/usuarios/{alumnoId}/cursos-inscriptos")
    Call<List<Map<String, Object>>> getCursosPorAlumno(@Path("alumnoId") Long alumnoId);

    @POST("/usuarios/baja-curso")
    Call<Map<String, Object>> darBajaDeCurso(@Body BajaCursoRequest request);

    @GET("/usuarios/{alumnoId}/cursos/{cursoId}/asistencias")
    Call<List<Asistencia>> getAsistenciasPorAlumnoYCurso(@Path("alumnoId") Long alumnoId, @Path("cursoId") Long cursoId);

    @POST("/usuarios/marcarPresente/{alumnoId}/{cursoId}")
    Call<Asistencia> marcarPresente(@Path("alumnoId") Long alumnoId, @Path("cursoId") Long cursoId
    );

    @GET("/usuarios/cursos/{cursoId}/materiales")
    Call<List<MaterialDeClase>> getMaterialesPorCurso(@Path("cursoId") Long cursoId);

    @GET("/usuarios/materiales/{materialId}/descargar")
    Call<ResponseBody> descargarMaterial(@Path("materialId") Long materialId);

    @POST("/usuarios/asistencias/marcarPresenteConFecha/{alumnoId}/{cursoId}/{fecha}")
    Call<Asistencia> marcarPresenteConFecha(
            @Path("alumnoId") Long alumnoId,
            @Path("cursoId") Long cursoId,
            @Path("fecha") String fechaISO
    );

    @POST("/usuarios/marcarAusenteConFecha/asistencias/{alumnoId}/{cursoId}/{fecha}")
    Call<Asistencia> marcarAusenteConFecha(
            @Path("alumnoId") Long alumnoId,
            @Path("cursoId") Long cursoId,
            @Path("fecha") String fechaISO
    );
}

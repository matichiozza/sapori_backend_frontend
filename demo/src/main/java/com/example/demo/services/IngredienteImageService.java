package com.example.demo.services;

import com.example.demo.datos.IngredienteDAO;
import com.example.demo.modelo.Ingrediente;

import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class IngredienteImageService {

    @Autowired
    private IngredienteDAO ingredienteDAO;

    private final String API_KEY = "Bearer e6LAQHEsQRf0ejajkmUhzhOxxROjRrH8WyXQCD6FJBk1PVqdQLRyIBds5qOytLZL";

    // Diccionario para traducir ingredientes comunes al inglés para buscar en Iconfinder
    private static final Map<String, String> traducciones = Map.ofEntries(
            Map.entry("aceite de oliva", "olive oil bottle"),
            Map.entry("aceite vegetal", "vegetable oil bottle"),
            Map.entry("aceite de coco", "coconut oil jar"),
            Map.entry("ajo", "garlic"),
            Map.entry("albahaca", "basil leaves"),
            Map.entry("almendras", "almonds"),
            Map.entry("azúcar", "sugar"),
            Map.entry("banano", "banana"),
            Map.entry("banana", "banana"),
            Map.entry("batata", "sweet potato"),
            Map.entry("berenjena", "eggplant"),
            Map.entry("brócoli", "broccoli"),
            Map.entry("camarones", "shrimp"),
            Map.entry("canela", "cinnamon stick"),
            Map.entry("cacao en polvo", "cocoa powder"),
            Map.entry("calabaza", "pumpkin"),
            Map.entry("carne de res", "beef"),
            Map.entry("cebolla", "onion"),
            Map.entry("champiñones", "mushrooms"),
            Map.entry("chili en polvo", "chili powder"),
            Map.entry("cilantro", "cilantro"),
            Map.entry("coca cola", "coca cola bottle"),
            Map.entry("comino", "cumin seeds"),
            Map.entry("crema de leche", "heavy cream"),
            Map.entry("crema agria", "sour cream"),
            Map.entry("eneldo", "dill"),
            Map.entry("espinaca", "spinach leaves"),
            Map.entry("filete de pescado", "fish fillet"),
            Map.entry("garbanzos", "chickpeas"),
            Map.entry("harina de trigo", "wheat flour"),
            Map.entry("huevo", "egg"),
            Map.entry("jengibre", "ginger root"),
            Map.entry("laurel", "bay leaf"),
            Map.entry("leche", "milk glass"),
            Map.entry("limón", "lemon"),
            Map.entry("lima", "lime"),
            Map.entry("lentejas", "lentils"),
            Map.entry("manzana", "apple"),
            Map.entry("mayonesa", "mayonnaise jar"),
            Map.entry("miel", "honey jar"),
            Map.entry("mostaza", "mustard jar"),
            Map.entry("nueces", "walnuts"),
            Map.entry("orégano", "oregano"),
            Map.entry("pan", "bread loaf"),
            Map.entry("pan rallado", "breadcrumbs"),
            Map.entry("paprika", "paprika"),
            Map.entry("pasta", "pasta noodles"),
            Map.entry("pavo", "turkey meat"),
            Map.entry("pescado", "fish"),
            Map.entry("pimiento rojo", "red bell pepper"),
            Map.entry("pimiento verde", "green bell pepper"),
            Map.entry("pimienta negra", "black peppercorn"),
            Map.entry("polenta", "polenta"),
            Map.entry("pollo", "chicken meat"),
            Map.entry("porotos", "beans"),
            Map.entry("queso parmesano", "parmesan cheese"),
            Map.entry("queso mozzarella", "mozzarella cheese"),
            Map.entry("quinoa", "quinoa"),
            Map.entry("romero", "rosemary"),
            Map.entry("sal", "salt"),
            Map.entry("salsa barbacoa", "barbecue sauce bottle"),
            Map.entry("salsa de soja", "soy sauce bottle"),
            Map.entry("salsa teriyaki", "teriyaki sauce bottle"),
            Map.entry("salmón", "salmon fillet"),
            Map.entry("semillas de sésamo", "sesame seeds"),
            Map.entry("sésamo", "sesame seeds"),
            Map.entry("tofu", "tofu block"),
            Map.entry("tomate", "tomato"),
            Map.entry("tomillo", "thyme"),
            Map.entry("vinagre", "vinegar bottle"),
            Map.entry("vino blanco", "white wine glass"),
            Map.entry("vino tinto", "red wine glass"),
            Map.entry("yogur", "yogurt cup"),
            Map.entry("zanahoria", "carrot"),
            Map.entry("zucchini", "zucchini"),
            Map.entry("ricota", "ricotta cheese"),
            Map.entry("arroz", "rice grains"),
            Map.entry("perejil", "parsley"),
            Map.entry("manteca", "butter stick"),
            Map.entry("vainilla", "vanilla bean"),
            Map.entry("chocolate", "chocolate bar"),
            Map.entry("ketchup", "ketchup bottle"),
            Map.entry("papa", "potato"),
            Map.entry("acelga", "chard"),
            Map.entry("coliflor", "cauliflower"),
            Map.entry("maíz", "corn cob"),
            Map.entry("pechuga de pollo", "chicken breast"),
            Map.entry("muslo de pollo", "chicken thigh"),
            Map.entry("atún en lata", "canned tuna"),
            Map.entry("calamar", "squid"),
            Map.entry("soja texturizada", "textured soy protein"),
            Map.entry("agua", "glass of water"),
            Map.entry("levadura", "baking yeast"),
            Map.entry("cerveza", "beer bottle"),
            Map.entry("pasas de uva", "raisins"),
            Map.entry("naranja", "orange"),
            Map.entry("cúrcuma", "turmeric powder"),
            Map.entry("ají molido", "ground chili pepper"),
            Map.entry("pesto", "basil pesto"),
            Map.entry("guacamole", "guacamole"),
            Map.entry("palta", "avocado"),
            Map.entry("hojas verdes", "green leaves"),
            Map.entry("rúcula", "arugula leaves"),
            Map.entry("couscous", "couscous"),
            Map.entry("chía", "chia seeds"),
            Map.entry("tortilla de trigo", "wheat tortilla")
    );

    public void completarImagenes() {
        List<Ingrediente> ingredientes = ingredienteDAO.getAllIngredientes();

        for (Ingrediente ing : ingredientes) {
            if (ing.getImagenUrl() == null || ing.getImagenUrl().isEmpty()) {
                try {
                    String nombreBusqueda = traducciones.getOrDefault(ing.getNombre().toLowerCase(), ing.getNombre());
                    String url = buscarImagen(nombreBusqueda);
                    if (url != null) {
                        ingredienteDAO.actualizarImagenUrlPorNombre(ing.getNombre(), url);
                        System.out.println("✅ Imagen agregada para: " + ing.getNombre());
                    } else {
                        System.out.println("⚠️ No se encontró imagen para: " + ing.getNombre());
                    }
                } catch (Exception e) {
                    System.out.println("❌ Error al procesar '" + ing.getNombre() + "': " + e.getMessage());
                }
            }
        }
    }

    private String buscarImagen(String nombre) throws Exception {
        String query = URLEncoder.encode(nombre, StandardCharsets.UTF_8);
        // Solicita solo iconos a color (multicolor), no premium y con un máximo de 1 resultado
        String urlStr = "https://api.iconfinder.com/v4/icons/search?query=" + query + "&count=1&premium=0&color=multi";

        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", API_KEY);
        // Agrego User-Agent para evitar bloqueos
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Error en la API: HTTP " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        con.disconnect();

        JSONObject json = new JSONObject(response.toString());
        JSONArray icons = json.getJSONArray("icons");
        if (icons.length() > 0) {
            JSONObject icon = icons.getJSONObject(0);
            JSONArray sizes = icon.getJSONArray("raster_sizes");
            // Tomamos el tamaño más grande disponible para mejor calidad
            JSONObject largestSize = sizes.getJSONObject(sizes.length() - 1);
            JSONArray formats = largestSize.getJSONArray("formats");
            // Usamos el primer formato disponible (PNG o SVG)
            return formats.getJSONObject(0).getString("preview_url");
        }

        return null;
    }
}

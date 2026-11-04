package com.empresa.anomalias.controller;

import com.empresa.anomalias.dto.ImportResultado;
import com.empresa.anomalias.service.DetectorAnomaliasService;
import com.empresa.anomalias.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/importar")
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;
    private final DetectorAnomaliasService detector;

    @GetMapping
    public String vista() { return "importar"; }

    @PostMapping
    public String subir(@RequestParam("archivo") MultipartFile archivo, Model m) {
        if (archivo.isEmpty()) {
            m.addAttribute("error", "Selecciona un archivo .xlsx, .xls o .csv");
            return "importar";
        }
        try {
            ImportResultado resultado = importService.importar(archivo);
            m.addAttribute("resultado", resultado);
            m.addAttribute("nuevasAnomalias", detector.analizar());
        } catch (Exception e) {
            m.addAttribute("error", "No se pudo leer el archivo: " + e.getMessage());
        }
        return "importar";
    }
}
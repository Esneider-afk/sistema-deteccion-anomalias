package com.empresa.anomalias.service;

import com.empresa.anomalias.dto.ImportResultado;
import com.empresa.anomalias.model.*;
import com.empresa.anomalias.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Formato esperado (primera fila = encabezados):
 * fecha | descripcion | monto | categoria | departamento | proveedor
 */
@Service
@RequiredArgsConstructor
public class ImportService {

    private final GastoRepository gastoRepo;
    private final CategoriaRepository categoriaRepo;
    private final DepartamentoRepository departamentoRepo;
    private final ProveedorRepository proveedorRepo;

    @Transactional
    public ImportResultado importar(MultipartFile archivo) throws IOException {
        String nombre = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename().toLowerCase();
        List<String[]> filas = nombre.endsWith(".csv") ? leerCsv(archivo) : leerExcel(archivo);

        int importados = 0;
        List<String> errores = new ArrayList<>();
        int numeroFila = 1; // la fila 1 es el encabezado

        for (String[] v : filas) {
            numeroFila++;
            if (String.join("", v).isBlank()) continue;
            try {
                if (v[1].isBlank() || v[3].isBlank() || v[4].isBlank() || v[5].isBlank()) {
                    throw new IllegalArgumentException("hay campos vacíos");
                }
                Gasto g = new Gasto();
                g.setFecha(parseFecha(v[0]));
                g.setDescripcion(v[1]);
                g.setMonto(parseMonto(v[2]));
                g.setCategoria(categoriaRepo.findByNombreIgnoreCase(v[3]).orElseGet(() -> {
                    Categoria c = new Categoria(); c.setNombre(v[3]); return categoriaRepo.save(c);
                }));
                g.setDepartamento(departamentoRepo.findByNombreIgnoreCase(v[4]).orElseGet(() -> {
                    Departamento d = new Departamento(); d.setNombre(v[4]); return departamentoRepo.save(d);
                }));
                g.setProveedor(proveedorRepo.findByNombreIgnoreCase(v[5]).orElseGet(() -> {
                    Proveedor p = new Proveedor(); p.setNombre(v[5]); return proveedorRepo.save(p);
                }));
                gastoRepo.save(g);
                importados++;
            } catch (Exception e) {
                errores.add("Fila " + numeroFila + ": " + e.getMessage());
            }
        }
        return new ImportResultado(importados, errores);
    }

    private List<String[]> leerCsv(MultipartFile f) throws IOException {
        String contenido = new String(f.getBytes(), StandardCharsets.UTF_8);
        if (contenido.startsWith("\uFEFF")) contenido = contenido.substring(1);
        String primera = contenido.lines().findFirst().orElse("");
        char separador = primera.contains(";") ? ';' : ','; // Excel en español guarda con ";"

        CSVFormat formato = CSVFormat.DEFAULT.builder()
                .setDelimiter(separador).setTrim(true).setIgnoreEmptyLines(true).build();

        List<String[]> filas = new ArrayList<>();
        try (CSVParser parser = formato.parse(new StringReader(contenido))) {
            boolean encabezado = true;
            for (CSVRecord r : parser) {
                if (encabezado) { encabezado = false; continue; }
                String[] v = new String[6];
                for (int i = 0; i < 6; i++) v[i] = i < r.size() ? r.get(i).trim() : "";
                filas.add(v);
            }
        }
        return filas;
    }

    private List<String[]> leerExcel(MultipartFile f) throws IOException {
        List<String[]> filas = new ArrayList<>();
        DataFormatter formateador = new DataFormatter();
        try (Workbook libro = WorkbookFactory.create(f.getInputStream())) {
            Sheet hoja = libro.getSheetAt(0);
            for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                if (fila == null) continue;
                String[] v = new String[6];
                for (int c = 0; c < 6; c++) {
                    Cell celda = fila.getCell(c);
                    if (celda == null) { v[c] = ""; continue; }
                    if (celda.getCellType() == CellType.NUMERIC) {
                        if (DateUtil.isCellDateFormatted(celda)) {
                            v[c] = celda.getLocalDateTimeCellValue().toLocalDate().toString();
                        } else {
                            v[c] = BigDecimal.valueOf(celda.getNumericCellValue()).toPlainString();
                        }
                    } else {
                        v[c] = formateador.formatCellValue(celda).trim();
                    }
                }
                filas.add(v);
            }
        }
        return filas;
    }

    private LocalDate parseFecha(String s) {
        for (DateTimeFormatter f : List.of(DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"), DateTimeFormatter.ofPattern("d/M/yyyy"))) {
            try { return LocalDate.parse(s.trim(), f); } catch (DateTimeParseException ignored) { }
        }
        throw new IllegalArgumentException("fecha inválida (" + s + ")");
    }

    private BigDecimal parseMonto(String raw) {
        try {
            String s = raw.replace("$", "").replace(" ", "").trim();
            if (s.contains(",") && s.contains(".")) s = s.replace(".", "").replace(",", ".");
            else if (s.contains(",")) s = s.replace(",", ".");
            BigDecimal monto = new BigDecimal(s);
            if (monto.signum() <= 0) throw new NumberFormatException();
            return monto;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("monto inválido (" + raw + ")");
        }
    }
}
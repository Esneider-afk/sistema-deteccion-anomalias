package com.empresa.anomalias.config;

import com.empresa.anomalias.model.*;
import com.empresa.anomalias.repository.*;
import com.empresa.anomalias.service.DetectorAnomaliasService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;
    private final DepartamentoRepository departamentos;
    private final ProveedorRepository proveedores;
    private final GastoRepository gastos;
    private final PasswordEncoder encoder;
    private final DetectorAnomaliasService detector;

    @Override
    public void run(String... args) {
        if (usuarios.count() == 0) {
            Usuario u = new Usuario();
            u.setNombre("Administrador");
            u.setEmail("admin@empresa.com");
            u.setPassword(encoder.encode("Admin123*"));
            usuarios.save(u);
        }
        if (categorias.count() > 0) return; // ya hay datos

        List<String> nombresCat = List.of("Viáticos", "Papelería", "Tecnología", "Publicidad", "Servicios públicos", "Mantenimiento");
        long[] base = {250_000, 80_000, 1_500_000, 900_000, 600_000, 400_000};
        List<Categoria> cats = nombresCat.stream().map(n -> { Categoria c = new Categoria(); c.setNombre(n); return categorias.save(c); }).toList();
        List<Departamento> deps = List.of("Administrativo", "Ventas", "Operaciones", "Sistemas").stream()
                .map(n -> { Departamento d = new Departamento(); d.setNombre(n); return departamentos.save(d); }).toList();
        List<Proveedor> provs = List.of("Distribuidora Andina", "TecnoSoft S.A.S.", "Publicitas Ltda.", "Servicios Generales S.A.", "Papelería Central", "Mantenimientos del Norte").stream()
                .map(n -> { Proveedor p = new Proveedor(); p.setNombre(n); return proveedores.save(p); }).toList();

        Random r = new Random(42);
        Gasto primero = null;
        for (int i = 0; i < 150; i++) {
            int ci = r.nextInt(cats.size());
            long monto = Math.round(base[ci] * (0.7 + r.nextDouble() * 0.6) / 1000.0) * 1000;
            Gasto g = nuevo(LocalDate.now().minusDays(r.nextInt(150)), "Gasto de " + nombresCat.get(ci).toLowerCase(),
                    BigDecimal.valueOf(monto), cats.get(ci), deps.get(r.nextInt(deps.size())), provs.get(ci));
            if (primero == null) primero = g;
        }

        // Casos anómalos de demostración
        nuevo(LocalDate.now().minusDays(3), "Compra inusual de papelería", BigDecimal.valueOf(4_800_000), cats.get(1), deps.get(0), provs.get(1));
        nuevo(LocalDate.now().minusDays(7), "Licencias de software", BigDecimal.valueOf(12_000_000), cats.get(2), deps.get(3), provs.get(2));
        nuevo(primero.getFecha(), primero.getDescripcion(), primero.getMonto(), primero.getCategoria(), primero.getDepartamento(), primero.getProveedor());

        detector.analizar();
    }

    private Gasto nuevo(LocalDate fecha, String desc, BigDecimal monto, Categoria c, Departamento d, Proveedor p) {
        Gasto g = new Gasto();
        g.setFecha(fecha); g.setDescripcion(desc); g.setMonto(monto);
        g.setCategoria(c); g.setDepartamento(d); g.setProveedor(p);
        return gastos.save(g);
    }
}
package com.empresa.anomalias.controller;

import com.empresa.anomalias.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @GetMapping("/")
    public String raiz() { return "redirect:/dashboard"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/registro")
    public String registro() { return "registro"; }

    @PostMapping("/registro")
    public String registrar(@RequestParam String nombre, @RequestParam String email,
                            @RequestParam String password, Model model, RedirectAttributes ra) {
        try {
            if (nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio");
            if (password.length() < 6) throw new IllegalArgumentException("La contraseña debe tener mínimo 6 caracteres");
            usuarioService.registrar(nombre, email, password);
            ra.addFlashAttribute("ok", "Cuenta creada correctamente. Ya puedes iniciar sesión.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }
}
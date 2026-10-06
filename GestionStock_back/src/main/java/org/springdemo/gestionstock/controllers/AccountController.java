package org.springdemo.gestionstock.controllers;

import jakarta.validation.Valid;
import org.springdemo.gestionstock.dto.AccountDto;
import org.springdemo.gestionstock.dto.AccountRequest;
import org.springdemo.gestionstock.services.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/accounts")
public class AccountController {
    private final AccountService accounts;
    public AccountController(AccountService accounts) { this.accounts = accounts; }
    @GetMapping public List<AccountDto> list() { return accounts.list(); }
    @PostMapping public ResponseEntity<AccountDto> create(@Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(201).body(accounts.create(request));
    }
    @PutMapping("/{source}/{id}")
    public AccountDto update(@PathVariable String source, @PathVariable Long id,
                             @Valid @RequestBody AccountRequest request, Authentication auth) {
        return accounts.update(source, id, request, auth.getName());
    }
    @DeleteMapping("/{source}/{id}")
    public ResponseEntity<Void> delete(@PathVariable String source, @PathVariable Long id, Authentication auth) {
        accounts.delete(source, id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}

package br.edu.ufvjm.mestre.identity.adapter.http;

import br.edu.ufvjm.mestre.identity.application.RegisterStudent;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {
    private final RegisterStudent registerStudent;
    public RegistrationController(RegisterStudent registerStudent) { this.registerStudent = registerStudent; }

    @PostMapping(path = "/api/v1/users", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@RequestBody RegistrationRequest request) {
        return RegistrationResponse.from(registerStudent.execute(request.getName(), request.getEmail(), request.getPassword()));
    }
}

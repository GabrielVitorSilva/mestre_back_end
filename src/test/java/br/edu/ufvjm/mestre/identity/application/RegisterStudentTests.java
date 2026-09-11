package br.edu.ufvjm.mestre.identity.application;

import br.edu.ufvjm.mestre.identity.domain.InvalidRegistration;
import br.edu.ufvjm.mestre.identity.domain.UserAccount;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegisterStudentTests {
    private final AccountStore store = mock(AccountStore.class);
    private final PasswordProtection passwords = mock(PasswordProtection.class);
    private final RegisterStudent register = new RegisterStudent(store, passwords);

    @Test
    void protectsPasswordBeforeStoringAndReturnsAccount() {
        when(passwords.hash("uma senha ficticia")).thenReturn("protected");
        var result = register.execute(" Aluna ", " A@EXAMPLE.COM ", "uma senha ficticia");
        var account = ArgumentCaptor.forClass(UserAccount.class);
        var order = inOrder(passwords, store);
        order.verify(passwords).hash("uma senha ficticia");
        order.verify(store).create(account.capture(), eq("protected"));
        assertSame(result, account.getValue());
        assertEquals("a@example.com", result.email());
    }
    @Test
    void invalidInputDoesNotHashOrPersist() {
        assertThrows(InvalidRegistration.class, () -> register.execute("", "a@example.com", "uma senha ficticia"));
        assertThrows(InvalidRegistration.class, () -> register.execute("Aluna", "invalid", "uma senha ficticia"));
        assertThrows(InvalidRegistration.class, () -> register.execute("Aluna", "a@example.com", "short"));
        verifyNoInteractions(store, passwords);
    }
    @Test
    void hashingFailureDoesNotPersist() {
        when(passwords.hash(anyString())).thenThrow(new IllegalStateException("failure"));
        assertThrows(IllegalStateException.class, () -> register.execute("Aluna", "a@example.com", "uma senha ficticia"));
        verifyNoInteractions(store);
    }
    @Test
    void propagatesExpectedConflict() {
        when(passwords.hash(anyString())).thenReturn("protected");
        doThrow(new EmailAlreadyRegistered()).when(store).create(any(), anyString());
        assertThrows(EmailAlreadyRegistered.class, () -> register.execute("Aluna", "a@example.com", "uma senha ficticia"));
    }
}

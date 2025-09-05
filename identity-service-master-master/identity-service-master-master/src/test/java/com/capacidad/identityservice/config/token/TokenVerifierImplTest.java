package com.capacidad.identityservice.config.token;

import com.capacidad.identityservice.misc.ApplicationProperties;
import com.capacidad.utils.exception.ExpiredTokenException;
import com.capacidad.utils.exception.InvalidTokenException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class TokenVerifierImplTest {

    @Mock
    private ApplicationProperties applicationProperties;
    @InjectMocks
    private TokenVerifierImpl tokenVerifier;

    @Before
    public void init() {
    }

    @Test(expected = InvalidTokenException.class)
    public void testVerifyThrowsInvalidTokenExceptionWhenKidDoesNotExist() {
        String invalidJwt = "eyJraWQiOiJ0ZXN0IiwiYWxnIjoiSFMyNTYiLCJ0eXAiOiJKV1QifQ.eyJzdWIiOiIxMjM0" +
                "NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.rkHiTfufnSIJ2VSfZtkD4SHGQj4Uqe1dwOz6NAiUcGE";

        tokenVerifier.verify(invalidJwt);
    }

    @Test(expected = InvalidTokenException.class)
    public void testVerifyThrowsInvalidTokenExceptionWhenKidExistButJwtHeadersAreInvalid() {
        String invalidJwt = "eyJraWQiOiJXMTVpWFhKTExFRXBMMkI0SjNZdVpqUkhXeklrT2lzdE9sNHpJV2s9IiwiYWxnIjoiSFMyNTYiLCJ0eXAiOiJKV1QifQ" +
                ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJpc3MiOiJpbnZhbGlkX2lzc3VlciJ9.5YrNMkDdCY8lsWatXoyO4b3rJso2F4vg4M5Gj24HOZU";

        tokenVerifier.verify(invalidJwt);
    }

    @Test
    public void testVerifyThrowsInvalidTokenExceptionWhenJwtIssuerIsInvalid() {
        String invalidJwt = "eyJraWQiOiI4MGFiZWNkMi1mOWI4LTQyNjAtYmIxNi02MmIzNDFiZDA1NmUiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdW" +
                "IiOiJhODQxNTZhOC0zNTUwLTRmN2UtOTJmNS1kODZiZDM4ZGI4NDciLCJyb2xlIjoiYWRtaW4iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0" +
                "cDovL2xvY2FsaG9zdDo4MDgxL3YxIiwiY2xpZW50X2lkIjoicHVibGljLWNsaWVudCIsImF1ZCI6InFrVWJJQUYyVTRITlVWVTdzeUZiY1FGT1RWU1h1RSIs" +
                "InNjb3BlIjpbImFsbDp1c2VycyJdLCJyZXNvdXJjZV9pZCI6IiIsImV4cCI6MTU1NzI2NzA0MCwiaWF0IjoxNTU3MjYzNDQwLCJ0ZW5hbnQiOiI5OTBkZTJmNi" +
                "0xM2EwLTQzYjMtODdjOS1hZWJkYWQ5YzJkZTg6SXByb1NTLXRlc3QiLCJlbWFpbCI6ImFkbWluZGV2QGNhcGFjaWRhZC5jb20uYXIiLCJ1c2VybmFtZSI6ImFkb" +
                "WluZGV2IiwiZ3JvdXAiOiJkZXYifQ.S_B2J5t7NLlkCxivcJVbs7PcnBzajNTRKnjZG15Y2McKXiVOI4meus3gMM0PJzoRQqysgbUydzbSZOUTif1QflWKdF0zw" +
                "Ze1SyHLTYIyfxYHBfRK8Tgl8DX6LXg52SR5ktZC4hA6O2kNzxJxfqEswvQFKxSMPZMugZJUcp2kFoLD1z6G5nX3gHU5brzsflX9p7ohpq7AB5KDVyyoHZ11fgkE" +
                "mF6YcHHlrrQqr3VGpyRM8LL6xOPRCg-6OijTbJmWB7YyD0XdctJnNVyF39YoELGGFjw_YeojnYNggNbytpDPSY0IRb3ldr2UVYtVUodfDoDq9kljdbQFjcmUP6Ox3g";

        when(applicationProperties.getJwtIssuer()).thenReturn("http://localhost:8081/v1");
        when(applicationProperties.getActiveProfile()).thenReturn("dev");

        InvalidTokenException thrown = (InvalidTokenException) catchThrowable(() -> tokenVerifier.verify(invalidJwt));

        assertThat(thrown.getMessage()).isEqualTo("tokenVerifier.generalError");
    }

    @Test(expected = ExpiredTokenException.class)
    public void testVerifyThrowsExpiredTokenExceptionWhenJwtIsExpired() {
        String expiredJwt = "eyJraWQiOiI4MGFiZWNkMi1mOWI4LTQyNjAtYmIxNi02MmIzNDFiZDA1NmUiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdW" +
                "IiOiJhODQxNTZhOC0zNTUwLTRmN2UtOTJmNS1kODZiZDM4ZGI4NDciLCJyb2xlIjoiYWRtaW4iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0" +
                "cDovL2xvY2FsaG9zdDo4MDgxL3YxIiwiY2xpZW50X2lkIjoicHVibGljLWNsaWVudCIsImF1ZCI6InFrVWJJQUYyVTRITlVWVTdzeUZiY1FGT1RWU1h1RSIs" +
                "InNjb3BlIjpbImFsbDp1c2VycyJdLCJyZXNvdXJjZV9pZCI6IiIsImV4cCI6MTU1NzI2NzA0MCwiaWF0IjoxNTU3MjYzNDQwLCJ0ZW5hbnQiOiI5OTBkZTJmNi" +
                "0xM2EwLTQzYjMtODdjOS1hZWJkYWQ5YzJkZTg6SXByb1NTLXRlc3QiLCJlbWFpbCI6ImFkbWluZGV2QGNhcGFjaWRhZC5jb20uYXIiLCJ1c2VybmFtZSI6ImFkb" +
                "WluZGV2IiwiZ3JvdXAiOiJkZXYifQ.S_B2J5t7NLlkCxivcJVbs7PcnBzajNTRKnjZG15Y2McKXiVOI4meus3gMM0PJzoRQqysgbUydzbSZOUTif1QflWKdF0zw" +
                "Ze1SyHLTYIyfxYHBfRK8Tgl8DX6LXg52SR5ktZC4hA6O2kNzxJxfqEswvQFKxSMPZMugZJUcp2kFoLD1z6G5nX3gHU5brzsflX9p7ohpq7AB5KDVyyoHZ11fgkE" +
                "mF6YcHHlrrQqr3VGpyRM8LL6xOPRCg-6OijTbJmWB7YyD0XdctJnNVyF39YoELGGFjw_YeojnYNggNbytpDPSY0IRb3ldr2UVYtVUodfDoDq9kljdbQFjcmUP6Ox3g";

        tokenVerifier.verify(expiredJwt);
    }

}

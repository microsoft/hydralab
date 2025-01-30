package com.microsoft.hydralab.center.util;

import com.microsoft.hydralab.center.util.SecretGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

public class SecretGeneratorTest {

    private SecretGenerator secretGenerator;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Before
    public void setUp() {
        secretGenerator = new SecretGenerator();
        bCryptPasswordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        secretGenerator.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Test
    public void testGenerateSecret() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String encodedUuid = "encodedUuid";
        Mockito.when(bCryptPasswordEncoder.encode(uuid)).thenReturn(encodedUuid);

        String result = secretGenerator.generateSecret();

        Assert.assertEquals(encodedUuid.replaceAll("\\W", ""), result);
    }
}
package no.nav.vedtak.felles.integrasjon.pdl;

import java.util.function.Supplier;

import no.nav.vedtak.felles.integrasjon.rest.RequestContextSupplier;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.SikkerhetContext;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

public final class TestContextSupplier implements RequestContextSupplier {

    private static final String DUMMY_TOKEN_OPENAM = "eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICIyb2c1RGk5ZW9LeFhOa3VPd0dvVUdBIiwgInN1YiI6ICJzMTQyNDQzIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICI1NTM0ZmQ4ZS03MmE2LTRhMWQtOWU5YS1iZmEzYThhMTljMDUtNjE2NjA2NyIsICJpc3MiOiAiaHR0cHM6Ly9pc3NvLXQuYWRlby5ubzo0NDMvaXNzby9vYXV0aDIiLCAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF1ZCI6ICJPSURDIiwgImNfaGFzaCI6ICJiVWYzcU5CN3dTdi0wVlN0bjhXLURnIiwgIm9yZy5mb3JnZXJvY2sub3BlbmlkY29ubmVjdC5vcHMiOiAiMTdhOGZiMzYtMGI0Ny00YzRkLWE4YWYtZWM4Nzc3Y2MyZmIyIiwgImF6cCI6ICJPSURDIiwgImF1dGhfdGltZSI6IDE0OTgwMzk5MTQsICJyZWFsbSI6ICIvIiwgImV4cCI6IDE0OTgwNDM1MTUsICJ0b2tlblR5cGUiOiAiSldUVG9rZW4iLCAiaWF0IjogMTQ5ODAzOTkxNSB9.S2DKQweQWZIfjaAT2UP9_dxrK5zqpXj8IgtjDLt5PVfLYfZqpWGaX-ckXG0GlztDVBlRK4ylmIYacTmEAUV_bRa_qWKRNxF83SlQRgHDSiE82SGv5WHOGEcAxf2w_d50XsgA2KDBCyv0bFIp9bCiKzP11uWPW0v4uIkyw2xVxMVPMCuiMUtYFh80sMDf9T4FuQcFd0LxoYcSFDEDlwCdRiF3ufw73qtMYBlNIMbTGHx-DZWkZV7CgukmCee79gwQIvGwdLrgaDrHFCJUDCbB1FFEaE3p3_BZbj0T54fCvL69aHyWm1zEd9Pys15yZdSh3oSSr4yVNIxhoF-nQ7gY-g;";
    private static final String DUMMY_TOKEN_TOKENX = "eyJraWQiOiI3Mzk2ZGIyZC1hN2MyLTQ1OGEtYjkzNC02ODNiNDgzYzUyNDIiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdF9oYXNoIjoiRzJ1Zl83OW1TTUhHSWFfNjFxTnJfUSIsInN1YiI6IjA5MDg4NDIwNjcyIiwidmVyIjoiMS4wIiwiaXNzIjoiaHR0cHM6XC9cL3Rva2VuZGluZ3MuZGV2LWdjcC5uYWlzLmlvIiwibm9uY2UiOiJWR1dyS1Zsa3RXZ3hCdTlMZnNnMHliMmdMUVhoOHRaZHRaVTJBdWdPZVl3IiwiY2xpZW50X2lkIjoiZGV2LWZzczp0ZWFtZm9yZWxkcmVwZW5nZXI6ZnBzb2tuYWQtbW90dGFrIiwiYXVkIjoiZGV2LWZzczp0ZWFtZm9yZWxkcmVwZW5nZXI6ZnBpbmZvIiwiYWNyIjoiTGV2ZWw0IiwibmJmIjoxNjE2Njg1NDA0LCJpZHAiOiJodHRwczpcL1wvbmF2dGVzdGIyYy5iMmNsb2dpbi5jb21cL2QzOGYyNWFhLWVhYjgtNGM1MC05ZjI4LWViZjkyYzEyNTZmMlwvdjIuMFwvIiwiYXV0aF90aW1lIjoxNjE2Njg1NDAyLCJleHAiOjE2MTY2ODU3MDQsImlhdCI6MTYxNjY4NTQwNCwianRpIjoiNGMwNzBmMGUtNzI0Ny00ZTdjLWE1OWEtYzk2Yjk0NWMxZWZhIn0.OvzjuabvPHG9nlRVc_KlCUTHOdfeT9GtBkASUGIoMayWGeIBDkr4-jc9gu6uT_WQqi9IJnvPkWgP3veqYHcOHpapD1yVNaQpxlrJQ04yP6N3gvkn-DcrBRDb3II_6qSaPQ_us2PJBDPq2VD5TGrNOL6EFwr8FK3zglYr-PgjW016ULTcmx_7gdHmbiC5PEn1_OtGNxzoUhSGKoD3YtUWP0qdsXzoKyeFL5FG9uZMSrDHHiJBZQFXGL9OzBU49Zb2K-iEPqa9m91O2JZGkhebfLjCAIPLPN4J68GFyfTvtNkZO71znorjo-e1nWxz53Wkj---RDY3JlIqNqzqHTfJgQ";

    public TestContextSupplier() {
    }

    @Override
    public Supplier<String> consumerIdFor(SikkerhetContext context) {
        return () -> "user";
    }

    @Override
    public Supplier<OpenIDToken> tokenFor(SikkerhetContext context) {
        return () -> new OpenIDToken(OpenIDProvider.TOKENX, new TokenString(DUMMY_TOKEN_TOKENX));
    }

    @Override
    public Supplier<OpenIDToken> stsSystemToken() {
        return () -> new OpenIDToken(OpenIDProvider.ISSO, new TokenString(DUMMY_TOKEN_OPENAM));
    }

    @Override
    public Supplier<OpenIDToken> azureSystemToken(String scope) {
        return () -> new OpenIDToken(OpenIDProvider.ISSO, new TokenString(DUMMY_TOKEN_OPENAM));
    }

    @Override
    public Supplier<String> consumerId() {
        return () -> "system";
    }

    @Override
    public Supplier<OpenIDToken> consumerToken() {
        return () -> new OpenIDToken(OpenIDProvider.ISSO, new TokenString(DUMMY_TOKEN_OPENAM));
    }
}

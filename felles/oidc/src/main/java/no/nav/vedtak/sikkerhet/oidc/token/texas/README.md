## Example claims fra de fleste type providere

### EntraID M2M:

```json
{
    "aud": "7c2c0c0f-4982-4042-9977-d882734b3cef",
    "iss": "https://login.microsoftonline.com/<tenant>/v2.0",
    "iat": 1772634468,
    "nbf": 1772634468,
    "exp": 1772638368,
    "aio": "ASQA2/8bAAAA47VW+xtF3MysgKGUu4Bg5WoqoX1B6UdaXgbkBM6Z1qI=",
    "azp": "a11e4cc6-1383-42c7-a714-30be1309cd6f",
    "azpacr": "2",
    "idtyp": "app",
    "oid": "0e9a8f43-09a5-47d2-a605-98a224cf9fc0",
    "rh": "1.AUcAcsVqlrf1vkuqiMdkGcD4UQ8MLHyCSUJAmXfYgnNLPO8AAABHAA.",
    "roles": [
        "access_as_application"
    ],
    "sub": "0e9a8f43-09a5-47d2-a605-98a224cf9fc0",
    "tid": "966ac572-f5b7-4bbe-aa88-c76419c0f851",
    "uti": "j9K8qtzOe0-5NvSq0PslAA",
    "ver": "2.0",
    "xms_ftd": "0DYHL_D3hgYIM-39oNEe_4LLvCfN9sp0byBc-MUzTUABZnJhbmNlYy1kc21z",
    "azp_name": "dev-gcp:nais:azure-token-generator"
}
```

### EntraID OBO:

```json
{
  "aud": "7c2c0c0f-4982-4042-9977-d882734b3cef",
  "iss": "https://login.microsoftonline.com/<tenant>/v2.0",
  "iat": 1772636594,
  "nbf": 1772636594,
  "exp": 1772641087,
  "aio": "AYQAe/8bAAAAgLj+c0+SxgCqKeUOX5BLv0LE3S3TCWKKWEQfUY1pH6X+2PSCYNQkCWae0Lufy92TnOwXOS2T3A1NEvx4i64F1fOklTFi7NcF7RJTChULHWh70H+eeBDFN47E7zk5fb4XW2MeymIkXsRENspXZDOwujBStY/xgRjkkMY52GgemE0=",
  "azp": "a11e4cc6-1383-42c7-a714-30be1309cd6f",
  "azpacr": "2",
  "groups": [
    "<gruppe_uuid>",
    "<gruppe_uuid>"
  ],
  "name": "<navn etternavn>",
  "oid": "d1096872-94a0-4ece-90ed-a1312aa51b82",
  "preferred_username": "<epost>",
  "rh": "1.AUcAcsVqlrf1vkuqiMdkGcD4UQ8MLHyCSUJAmXfYgnNLPO_kAP9HAA.",
  "scp": "defaultaccess",
  "sid": "002e6aea-d45b-d181-4a4b-23d208bcb1c4",
  "sub": "2_yqiXSyq70ix5WghkQEvOcqGefdrEADXESRl6qKXwk",
  "tid": "966ac572-f5b7-4bbe-aa88-c76419c0f851",
  "uti": "K800VlEfiUGTwPDLfn0qAA",
  "ver": "2.0",
  "xms_ftd": "3M3iHOGv4VDTptHcRS3DuNY2qe6UxllDaGWK_FlttlkBc3dlZGVuYy1kc21z",
  "NAVident": "<navident>",
  "azp_name": "dev-gcp:nais:azure-token-generator"
}
```

## TokenX:

```json
{
    "sub": "f085c5b4-73e4-4045-afc0-edf88489c94e",
    "client_amr": "private_key_jwt",
    "iss": "https://tokenx.dev-gcp.nav.cloud.nais.io",
    "pid": "<fødselsnummer>",
    "client_id": "dev-gcp:nais:tokenx-token-generator",
    "tid": "default",
    "aud": "dev-gcp:teamforeldrepenger:fpinntektsmelding",
    "acr": "idporten-loa-high",
    "nbf": 1772637477,
    "idp": "https://fakedings.intern.dev.nav.no/fake",
    "scope": "openid",
    "exp": 1772641077,
    "iat": 1772637477,
    "consumer": {
        "authority": "iso6523-actorid-upis",
        "ID": "0192:<orgnummer>"
    },
    "jti": "9f18a58a-9c47-4318-99f0-e5616964d1d9"
}
```

## Maskinporten RAR:

```json
{
    "authorization_details": [
        {
            "type": "urn:altinn:systemuser",
            "systemuser_org": {
                "authority": "iso6523-actorid-upis",
                "ID": "0192:<orgnummer>"
            },
            "systemuser_id": [
                "28adb41e-4e93-469d-84db-db41943af6ac"
            ],
            "system_id": "<system_identifikator>"
        }
    ],
    "scope": "nav:inntektsmelding/foreldrepenger nav:helseytelser/sykepenger",
    "iss": "https://test.maskinporten.no/",
    "client_amr": "private_key_jwt",
    "token_type": "Bearer",
    "exp": 1772635010,
    "iat": 1772634410,
    "client_id": "105f1576-0e36-47f0-b5bf-01e33c1d6c97",
    "jti": "9vd322FMLOY0WxbQ_BFvsHylw7K0cj6f9TqS93fdT2w",
    "consumer": {
        "authority": "iso6523-actorid-upis",
        "ID": "0192:<orgnummer>"
    }
}
```

## Maskinporten:

```json
{
  "scope": "nav:inntektsmelding/foreldrepenger nav:helseytelser/sykepenger",
  "iss": "https://test.maskinporten.no/",
  "client_amr": "private_key_jwt",
  "token_type": "Bearer",
  "exp": 1772638438,
  "iat": 1772637838,
  "client_id": "105f1576-0e36-47f0-b5bf-01e33c1d6c97",
  "jti": "SVx7V2HaNEQzO0ZLe8n4UadVl2IigVL-UHu8t2RwfvE",
  "consumer": {
    "authority": "iso6523-actorid-upis",
    "ID": "0192:<orgnummer>"
  }
}
```

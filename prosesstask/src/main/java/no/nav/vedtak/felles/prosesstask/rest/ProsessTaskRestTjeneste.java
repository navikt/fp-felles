package no.nav.vedtak.felles.prosesstask.rest;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.DRIFT;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.felles.prosesstask.rest.app.ProsessTaskApplikasjonTjeneste;
import no.nav.vedtak.felles.prosesstask.rest.dto.FeiletProsessTaskDataDto;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskDataDto;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskDataInfo;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskDataPayloadDto;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskIdDto;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskOpprettInputDto;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskRestartInputDto;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskRestartResultatDto;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskRetryAllResultatDto;
import no.nav.vedtak.felles.prosesstask.rest.dto.SokeFilterDto;
import no.nav.vedtak.log.sporingslogg.Sporingsdata;
import no.nav.vedtak.log.sporingslogg.SporingsloggHelper;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@OpenAPIDefinition(tags = @Tag(name = "prosesstask", description = "Håndtering av asynkrone oppgaver i form av prosesstask"))
@Path("/prosesstask")
@RequestScoped
@Transaction
public class ProsessTaskRestTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(ProsessTaskRestTjeneste.class);

    private ProsessTaskApplikasjonTjeneste prosessTaskApplikasjonTjeneste;

    public ProsessTaskRestTjeneste() {
        // REST CDI
    }

    @Inject
    public ProsessTaskRestTjeneste(ProsessTaskApplikasjonTjeneste prosessTaskApplikasjonTjeneste) {
        this.prosessTaskApplikasjonTjeneste = prosessTaskApplikasjonTjeneste;
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
        description = "Oppretter en prosess task i henhold til request",
        summary = "Oppretter en ny task klar for kjøring.",
        tags = "prosesstask",
        responses = {
            @ApiResponse(responseCode = "202", description = "Prosesstaskens oppdatert informasjon"),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil eller tekniske/funksjonelle feil")
        }
    )
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT)
    public ProsessTaskDataDto createProsessTask(@Parameter(description = "Informasjon for restart en eksisterende prosesstask") @Valid ProsessTaskOpprettInputDto inputDto) {
        //kjøres manuelt for å avhjelpe feilsituasjon, da er det veldig greit at det blir logget!
        logger.info("Oppretter prossess task av type {}", inputDto.getTaskType());

        return prosessTaskApplikasjonTjeneste.opprettTask(inputDto);
    }

    @POST
    @Path("/launch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Restarter en eksisterende prosesstask.",
        summary = "En allerede FERDIG prosesstask kan ikke restartes. En prosesstask har normalt et gitt antall forsøk den kan kjøres automatisk. " +
            "Dette endepunktet vil tvinge tasken til å trigge uavhengig av maks antall forsøk",
        tags = "prosesstask",
        responses = {
            @ApiResponse(responseCode = "200", description = "Prosesstaskens oppdatert informasjon",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProsessTaskRestartResultatDto.class))),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil eller tekniske/funksjonelle feil")
        }
    )
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT)
    public ProsessTaskRestartResultatDto restartProsessTask(@Parameter(description = "Informasjon for restart en eksisterende prosesstask") @Valid ProsessTaskRestartInputDto restartInputDto) {
        //kjøres manuelt for å avhjelpe feilsituasjon, da er det veldig greit at det blir logget!
        logger.info("Restarter prossess task {}", restartInputDto.getProsessTaskId());

        return prosessTaskApplikasjonTjeneste.flaggProsessTaskForRestart(restartInputDto);
    }

    @POST
    @Path("/retryall")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Restarter alle prosesstask med status FEILET.",
        summary = "Dette endepunktet vil tvinge feilede tasks til å trigge ett forsøk uavhengig av maks antall forsøk",
        tags = "prosesstask",
        responses = {
            @ApiResponse(responseCode = "200", description = "Response med liste av prosesstasks som restartes",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProsessTaskRetryAllResultatDto.class))),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil eller tekniske/funksjonelle feil")
        }
    )
    @BeskyttetRessurs(action = CREATE, ressurs = DRIFT)
    public ProsessTaskRetryAllResultatDto retryAllProsessTask() {
        //kjøres manuelt for å avhjelpe feilsituasjon, da er det veldig greit at det blir logget!
        logger.info("Restarter alle prossess task i status FEILET");

        return prosessTaskApplikasjonTjeneste.flaggAlleFeileteProsessTasksForRestart();
    }

    @POST
    @Path("/list")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Søker etter prosesstask med mulighet for filtrert søk.",
        tags = "prosesstask",
        responses = {
            @ApiResponse(responseCode = "200", description = "Liste over prosesstasker, eller tom liste når angitt/default søkefilter ikke finner noen prosesstasker",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProsessTaskDataDto.class)))
        }
    )
    @BeskyttetRessurs(action = READ, ressurs = DRIFT)
    public List<ProsessTaskDataDto> finnProsessTasks(@Parameter(description = "Søkefilter for å begrense resultatet av returnerte prosesstask.") @Valid SokeFilterDto sokeFilterDto) {
        List<ProsessTaskDataDto> resultat = prosessTaskApplikasjonTjeneste.finnAlle(sokeFilterDto);

        //må logge tilgang til personopplysninger, det blir ikke logget nok via @BeskyttetRessurs siden det er rolle-tilgang her
        for (ProsessTaskDataDto dto : resultat) {
            loggLesingAvPersondataFraProsessTask(dto, "/list");
        }

        return resultat;
    }

    @POST
    @Path("/feil")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter informasjon om feilet prosesstask med angitt prosesstask-id",
        tags = "prosesstask",
        responses = {
            @ApiResponse(responseCode = "200", description = "Angit prosesstask-id finnes",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = FeiletProsessTaskDataDto.class))),
            @ApiResponse(responseCode = "404", description = "Tom respons når angitt prosesstask-id ikke finnes"),
            @ApiResponse(responseCode = "400", description = "Feil input")
        }
    )
    @BeskyttetRessurs(action = READ, ressurs = DRIFT)
    public Response finnFeiletProsessTask(@NotNull @Parameter(description = "Prosesstask-id for feilet prosesstask") @Valid ProsessTaskIdDto prosessTaskIdDto) {
        Optional<FeiletProsessTaskDataDto> resultat = prosessTaskApplikasjonTjeneste.finnFeiletProsessTask(prosessTaskIdDto.getProsessTaskId());
        if (resultat.isPresent()) {

            //må logge tilgang til personopplysninger, det blir ikke logget nok via @BeskyttetRessurs siden det er rolle-tilgang her
            loggLesingAvPersondataFraProsessTask(resultat.get(), "/no/nav/vedtak/felles/behandlingsprosess/prosesstask/rest/feil");

            return Response.ok(resultat.get()).build();
        }
        return Response.status(HttpStatus.SC_NOT_FOUND).build();
    }

    @POST
    @Path("/payload")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(description = "Henter informasjon om prosesstask, inkludert payload for angitt prosesstask-id",
        tags = "prosesstask",
        responses = {
            @ApiResponse(responseCode = "200", description = "Angit prosesstask-id finnes",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProsessTaskDataPayloadDto.class))),
            @ApiResponse(responseCode = "404", description = "Tom respons når angitt prosesstask-id ikke finnes"),
            @ApiResponse(responseCode = "400", description = "Feil input")
        }
    )
    @BeskyttetRessurs(action = READ, ressurs = DRIFT)
    public Response finnProsessTaskInkludertPayload(@NotNull @Parameter(description = "Prosesstask-id for en eksisterende prosesstask") @Valid ProsessTaskIdDto prosessTaskIdDto) {
        Optional<ProsessTaskDataPayloadDto> resultat = prosessTaskApplikasjonTjeneste.finnProsessTaskMedPayload(prosessTaskIdDto.getProsessTaskId());
        if (resultat.isPresent()) {

            //må logge tilgang til personopplysninger, det blir ikke logget nok via @BeskyttetRessurs siden det er rolle-tilgang her
            loggLesingAvPersondataFraProsessTask(resultat.get(), "/payload");

            return Response.ok(resultat.get()).build();
        }
        return Response.status(HttpStatus.SC_NOT_FOUND).build();
    }

    private void loggLesingAvPersondataFraProsessTask(ProsessTaskDataInfo prosessTaskInfo, String metode) {
        String actionType = "read";
        String endepunkt = ProsessTaskRestTjeneste.class.getAnnotation(Path.class).value() + metode;
        Optional<Sporingsdata> sporingsdata = prosessTaskInfo.lagSporingsloggData(metode);
        sporingsdata.ifPresent(sd -> SporingsloggHelper.logSporing(ProsessTaskRestTjeneste.class, sd, actionType, endepunkt));
    }
}

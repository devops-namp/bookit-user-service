package uns.ac.rs.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.distribution.Histogram;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Provider
@ApplicationScoped
public class HttpMetricsFilter implements ContainerRequestFilter, ContainerResponseFilter, ExceptionMapper<WebApplicationException> {

    @Inject
    RoutingContext context;

    @Inject
    MeterRegistry registry;

    private final Counter totalRequests;
    private final Counter successRequests;
    private final Counter failedRequests;
    private final Counter uniqueVisitors;
    private final Counter notFoundRequests;
    private final DistributionSummary requestSize;
    private final DistributionSummary responseSize;

    private final Set<String> uniqueVisitorSet = new HashSet<>();

    @Inject
    public HttpMetricsFilter(MeterRegistry registry) {
        this.registry = registry;
        totalRequests = registry.counter("http_requests_total", "description", "Total HTTP requests");
        successRequests = registry.counter("http_requests_success_total", "description", "Successful HTTP requests");
        failedRequests = registry.counter("http_requests_failed_total", "description", "Failed HTTP requests");
        uniqueVisitors = registry.counter("unique_visitors_total", "description", "Unique visitors");
        notFoundRequests = registry.counter("http_requests_404_total", "description", "404 Not Found requests");
        requestSize = DistributionSummary.builder("http_request_size_bytes").description("HTTP request sizes in bytes").register(registry);
        responseSize = DistributionSummary.builder("http_response_size_bytes").description("HTTP response sizes in bytes").register(registry);
    }


    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        totalRequests.increment();
        if (requestContext.getLength() != -1) {
            requestSize.record(requestContext.getLength());
        }
        String visitor = getVisitorId();
        if (!uniqueVisitorSet.contains(visitor)) {
            System.out.println("Unique visitor: " + visitor);
            uniqueVisitorSet.add(visitor);
            uniqueVisitors.increment();
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        int status = responseContext.getStatus();
        if (responseContext.getLength() != -1) {
            responseSize.record(responseContext.getLength());
        }

        if (status >= 200 && status < 400) {
            successRequests.increment();
        } else {
            failedRequests.increment();
        }

        if (status == 404) {
            notFoundRequests.increment();
        }
    }

    @Override
    public Response toResponse(WebApplicationException exception) {
        if (exception instanceof NotFoundException) {
            notFoundRequests.increment();
        } else {
            failedRequests.increment();
        }
        return exception.getResponse();
    }


    private String getVisitorId() {
        String ip = context.request().remoteAddress().toString();
        String timeStamp = Long.toString(System.currentTimeMillis());
        String id = context.request().getHeader("User-Agent") + ":" + ip + ":" + timeStamp;
        return id;
    }
}

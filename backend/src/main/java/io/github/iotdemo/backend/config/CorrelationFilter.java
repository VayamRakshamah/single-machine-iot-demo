package io.github.iotdemo.backend.config;
import jakarta.servlet.*;import jakarta.servlet.http.*;import org.slf4j.MDC;import org.springframework.stereotype.Component;
import java.io.IOException;import java.util.UUID;
@Component
public class CorrelationFilter extends OncePerRequestFilterBase {
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{String id=OptionalHeader.value(req.getHeader("X-Correlation-ID"));MDC.put("correlationId",id);res.setHeader("X-Correlation-ID",id);try{chain.doFilter(req,res);}finally{MDC.remove("correlationId");}}
 private static class OptionalHeader{static String value(String value){return value==null||value.isBlank()?UUID.randomUUID().toString():value.substring(0,Math.min(value.length(),128));}}
}
abstract class OncePerRequestFilterBase extends org.springframework.web.filter.OncePerRequestFilter {}

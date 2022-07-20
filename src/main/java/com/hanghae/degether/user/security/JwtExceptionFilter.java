package com.hanghae.degether.user.security;

import com.hanghae.degether.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {



    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        try {
            chain.doFilter(req, res); // go to 'JwtAuthenticationFilter'
        } catch (ExpiredJwtException ex) {
            setErrorResponse(res, ErrorCode.EXPIRED_TOKEN);
        }catch (SignatureException ex){
            setErrorResponse(res,ErrorCode.SIGNATURE_TOKEN);
        }catch (UnsupportedJwtException ex){
            setErrorResponse(res,ErrorCode.UNSUPPORT_TOKEN);
        }catch (IllegalArgumentException ex) {
            setErrorResponse(res, ErrorCode.UNAUTHORIZED_TOKEN);
        }catch (MalformedJwtException ex){
            setErrorResponse(res,ErrorCode.MALFORMED_TOKEN);
        }

    }

    public void setErrorResponse(HttpServletResponse res, ErrorCode ex) throws IOException {
        res.setCharacterEncoding("UTF-8");
        res.setStatus(ex.getCode());
        res.setContentType("application/json; charset=UTF-8");

        res.getWriter().write(ex.getMessage());
        logger.error(ex.getMessage());
    }

}

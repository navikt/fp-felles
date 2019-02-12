package no.nav.vedtak.sikkerhetsfilter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

@WebFilter(urlPatterns = "/*")
public final class XSSFilter implements Filter {

    static class FilteredRequest extends HttpServletRequestWrapper {

        public FilteredRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String paramName) {//ikke i bruk??
            String value = super.getParameter(paramName);
            value = hvitvaskKunBokstaver(value);
            return value;
        }

        @Override
        public String[] getParameterValues(String paramName) {//ikke i bruk??
            String[] values = super.getParameterValues(paramName);
            if(null == values) {
                return null;
            }

            for (int index = 0; index < values.length; index++) {
                values[index] = hvitvaskKunBokstaver(values[index]);
            }
            return values;
        }

        @Override
        public String getQueryString() {
            return hvitvaskBokstaverOgVanligeTegn(super.getQueryString());
        }

        @Override
        public Cookie[] getCookies() {
            Cookie[] cookies = super.getCookies();
            if (cookies != null) {
                for (Cookie cooky : cookies) {
                    cooky.setValue(hvitvaskCookie(cooky.getValue())); //NOSONAR
                }
            }
            return cookies;
        }

        private String hvitvaskKunBokstaver(String unsanitizedString) {
            return SimpelHvitvasker.hvitvaskKunBokstaver(unsanitizedString);
        }

        private String hvitvaskBokstaverOgVanligeTegn(String unsanitizedString) {
            return SimpelHvitvasker.hvitvaskBokstaverOgVanligeTegn(unsanitizedString);
        }

        private String hvitvaskCookie(String unsanitizedString) {
            return SimpelHvitvasker.hvitvaskCookie(unsanitizedString);
        }

        @Override
        public HttpSession getSession() {
            return getSession(true);
        }

        @Override
        public HttpSession getSession(boolean create) {
            if (create) {
                throw new IllegalArgumentException("This is a stateless application so creating a Session is forbidden.");
            }
            return super.getSession(create);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        //Denne er her kun fordi den er påkravd
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        chain.doFilter(new FilteredRequest((HttpServletRequest) request), response);//NOSONAR //$NON-NLS-1$
    }


    @Override
    public void destroy() {
        //Påkrevd av interface
    }
}

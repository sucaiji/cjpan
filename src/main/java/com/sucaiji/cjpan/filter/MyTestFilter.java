package com.sucaiji.cjpan.filter;

import org.springframework.context.annotation.ComponentScan;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter(filterName = "testFilter1",urlPatterns = "/*")
public class MyTestFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("初始化filter---------------");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("执行filter里面的方法-------------");
    }

    @Override
    public void destroy() {
        System.out.println("销毁filter----------");
    }
}

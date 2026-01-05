package com.ankit61;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.*;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String msg = request.getParameter("message");

        String history = (String) session.getAttribute("history");
        if (history == null) history = "";

        history += "User: " + msg + "\n";

        String reply = null;
        try {
            reply = OpenAIClient.askGPT(history);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        history += "Bot: " + reply + "\n";
        session.setAttribute("history", history);

        response.setContentType("text/plain");
        response.getWriter().print(reply);
    }
}

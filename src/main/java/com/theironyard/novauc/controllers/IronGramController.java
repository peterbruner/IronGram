package com.theironyard.novauc.controllers;


import com.theironyard.novauc.entities.Photo;
import com.theironyard.novauc.entities.User;
import com.theironyard.novauc.services.PhotoRepository;
import com.theironyard.novauc.services.UserRepository;
import com.theironyard.novauc.utilities.PasswordStorage;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@RestController
public class IronGramController {
    @Autowired
    UserRepository users;

    @Autowired
    PhotoRepository photos;

    Server dbui = null;

    @PostConstruct
    public void init() throws SQLException {
        dbui = Server.createWebServer().start();
    }

    @PreDestroy
    public void destroy() {
        dbui.stop();
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public User login(String username, String password, HttpSession session, HttpServletResponse response) throws Exception {
        User user = users.findFirstByName(username);
        if (user == null) {
            user = new User(username, PasswordStorage.createHash(password));
            users.save(user);
        } else if (!PasswordStorage.verifyPassword(password, user.getPassword())) {
            throw new Exception("Wrong password");
        }
        session.setAttribute("username", username);
        response.sendRedirect("/");
        return user;
    }

    @RequestMapping("/logout")
    public void logout(HttpSession session, HttpServletResponse response) throws IOException {
        session.invalidate();
        response.sendRedirect("/");
    }

    @RequestMapping(path = "/user", method = RequestMethod.GET)
    public User getUser(HttpSession session) {
        String username = (String) session.getAttribute("username");
        return users.findFirstByName(username);
    }

    @RequestMapping("/upload")
    public Photo upload(
            HttpSession session,
            HttpServletResponse response,
            String recipient,
            long seconds,
            boolean checkbox,
            MultipartFile photo
    ) throws Exception {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new Exception("Not logged in.");
        }

        User sendUser = users.findFirstByName(username);
        User recipientUser = users.findFirstByName(recipient);

        if (recipientUser == null) {
            throw new Exception("Recipient name doesn't exist.");
        }

        if (!photo.getContentType().startsWith("image")) {
            throw new Exception("Only images are allowed");
        }

        File dir = new File("public");
        dir.mkdirs();
        File photoFile = File.createTempFile("photo", photo.getOriginalFilename(), dir /*new File("public")*/);
        FileOutputStream fos = new FileOutputStream(photoFile);
        fos.write(photo.getBytes());

        Photo p = new Photo();
        p.setSender(sendUser);
        p.setRecipient(recipientUser);
        p.setFilename(photoFile.getName());
        p.setSeconds(seconds);
        p.setCheckbox(checkbox);
        photos.save(p);

        response.sendRedirect("/");

        return p;
    }

    @RequestMapping(path = "/photos", method = RequestMethod.GET)
    public List<Photo> showPhotos(HttpSession session) throws Exception {
        String username = (String) session.getAttribute("username");
        //Long seconds = (Long) session.getAttribute("seconds");
        if (username == null) {
            throw new Exception("Not logged in.");
        }
        User user = users.findFirstByName(username);
        //File file = new File("public");

        time(photos.findByRecipient(user));

        return photos.findByRecipient(user);
    }

    public void deletePhoto(Photo photo) {
        photos.delete(photo); //delete the item from database
        File f = new File("public/" + photo.getFilename());
        f.delete();
    }

    public void time(List<Photo> allPhotos) {
        for (Photo photo: allPhotos) {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    deletePhoto(photo);
                }
            };
            timer.schedule(timerTask, photo.getSeconds() * 1000);
        }
    }

    @RequestMapping("/public-photos/{name}")
    public List<Photo> publicPhotos(@PathVariable("name") String name) {
        return photos.findAllBySenderAndCheckbox(
                users.findFirstByName(name), false
        );
    }



}

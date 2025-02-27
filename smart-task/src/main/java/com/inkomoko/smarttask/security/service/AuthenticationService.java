package com.inkomoko.smarttask.security.service;

import com.inkomoko.smarttask.user.Enums.Gender;
import com.inkomoko.smarttask.user.dtos.UserDto;
import com.inkomoko.smarttask.user.models.*;
import com.inkomoko.smarttask.user.repository.*;
import com.inkomoko.smarttask.email.dtos.EmailDto;
import com.inkomoko.smarttask.email.services.EmailService;
import com.inkomoko.smarttask.helpers.LogHelper;
import com.inkomoko.smarttask.helpers.StringHelper;
import com.inkomoko.smarttask.security.Enums.ResponseStatus;
import com.inkomoko.smarttask.security.dto.AuthenticationRequest;
import com.inkomoko.smarttask.security.dto.AuthenticationResponse;
import com.inkomoko.smarttask.security.dto.ChangePasswordRequest;
import com.inkomoko.smarttask.security.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final EmailService emailService;
    private final EmailVerifyRepository emailVerifyRepository;

    public AuthenticationResponse register(RegisterRequest request) {
        System.out.println(request.toString());
        request.setPhoneNumber(StringHelper.getCleanPhone(request.getPhoneNumber()));

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (request.getGender().isEmpty() || request.getEmail().isEmpty() || request.getPhoneNumber().isEmpty() || request.getPassword().isEmpty()) {
            return AuthenticationResponse.builder().responseStatus(ResponseStatus.FAILURE).message("Invalid request").build();
        }

        if (user != null) {
            return AuthenticationResponse.builder().responseStatus(ResponseStatus.FAILURE).message("Email already exists!").build();
        }

        user = userRepository.findByPhoneNumber(request.getPhoneNumber()).orElse(null);

        if (user != null) {
            return AuthenticationResponse.builder().responseStatus(ResponseStatus.FAILURE).message("Phone number already exists!").build();
        }

        UserRole guestRole = userRoleRepository.findByAlias("guest");


        if (guestRole == null) {
            guestRole = this.userRoleRepository.save(
                    UserRole.builder().name("Guest").alias("guest").status(1).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).createdBy(0).updatedBy(0).build()
            );
        }

        JSONObject info = new JSONObject();
        info.put("avatar", generateAvatarFromName(request.getFirstName() + " " + request.getLastName()));
        try {
            user = User.builder()
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .information(info.toString())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .userRole(guestRole)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .createdBy(0)
                    .status(1)
                    .updatedBy(0)
                    .phoneNumber(request.getPhoneNumber())
                    .gender(Gender.valueOf(request.getGender().equals("Male") ? "M" : "F"))
                    .birthDate(LocalDate.parse(request.getBirthDate())).build();
            User saved = userRepository.save(user);
            if (saved.getId() != 0) {
                userCredentialRepository.save(
                        UserCredential.builder()
                                .user(saved)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .createdBy(saved.getId())
                                .updatedBy(saved.getId())
                                .attempt(0)
                                .passwordHistory(saved.getPassword())
                                .attemptedAt(LocalDateTime.now())
                                .isNew(1)
                                .lastLogin(LocalDateTime.now())
                                .build()
                );
            }
            LogHelper.logger.error(saved + "......");
            String token = jwtService.generateToken(user);

            return AuthenticationResponse.builder()
                    .email(saved.getEmail())
                    .firstName(saved.getFirstName())
                    .lastName(saved.getLastName())
                    .role(saved.getUserRole())
                    .status("SUCCESS")
                    .token(token)
                    .message("User has been registered successfully!")
                    .responseStatus(ResponseStatus.SUCCESS)
                    .birthDate(saved.getBirthDate())
                    .gender(saved.getGender())
                    .phoneNumber(saved.getPhoneNumber())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            LogHelper.logger.error("Error: " + e.getMessage());

            return AuthenticationResponse.builder().responseStatus(ResponseStatus.FAILURE).message("Something went wrong!").build();
        }
    }

    public Object authenticate(AuthenticationRequest request, HttpServletRequest httpServletRequest) {
        System.out.println(request.toString());
        String userId = request.getEmail();

        HashMap response = new HashMap();

        User user = userRepository.findAll((root, query, criteriaBuilder) -> criteriaBuilder.and(criteriaBuilder.equal(root.get("email"), request.getEmail()), criteriaBuilder.notEqual(root.get("status"), 0))).stream().findFirst().orElse(null);

        System.out.println("User null 0");

        if (user == null) {

            System.out.println("User null");

            response.put("status", ResponseStatus.FAILURE);
            response.put("message", "Invalid email or password!");

            return response;
        }

        Map<String, Object> validationResponse = validateCredentials(user, request.getPassword());

        if (validationResponse.get("status").toString().equals("FAILURE")) {

            response.put("status", ResponseStatus.FAILURE);
            response.put("message", "Invalid email or password!");

            return response;
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        user.setUpdatedAt(LocalDateTime.now());
//        if (user.getInformation() == null || new JSONObject(user.getInformation()).get("avatar") == null) {
//            JSONObject information = new JSONObject();
//            information.put("avatar", generateAvatarFromName(user.getFirstName() + " " + user.getLastName()));
//            user.setInformation(information.toString());
//        }
        User saved = userRepository.save(user);

        String token = jwtService.generateToken(user);

        return AuthenticationResponse.builder().email(user.getEmail()).id(user.getId()).firstName(user.getFirstName()).lastName(user.getLastName()).role(user.getUserRole()).status("SUCCESS").token(token).message("Login was successful!").responseStatus(ResponseStatus.SUCCESS).createdAt(user.getCreatedAt()).birthDate(user.getBirthDate()).gender(user.getGender()).phoneNumber(user.getPhoneNumber()).build();
    }

    public AuthenticationResponse checkEmailAndPhoneNumber(String email, String phoneNumber) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            return AuthenticationResponse.builder().responseStatus(ResponseStatus.FAILURE).message("Email already exists!").build();
        }

        user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);

        if (user != null) {
            return AuthenticationResponse.builder().responseStatus(ResponseStatus.FAILURE).message("Phone number already exists!").build();
        }

        return AuthenticationResponse.builder().responseStatus(ResponseStatus.SUCCESS).build();
    }

    private String generateAvatarFromName(String fullName) {
        String text = fullName;
        int width = 300;
        int height = 300;

        // Create a buffered image with a solid background color
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Set background color (red) and fill the image
        g2d.setColor(new Color(239, 64, 53)); // Adjust the color as needed
        g2d.fillRect(0, 0, width, height);

        // Set font and text color
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 30)); // Adjust font size if necessary
        g2d.setColor(Color.WHITE);

        // Measure the text size to center it
        FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height - fm.getHeight()) / 2 + fm.getAscent();

        // Draw the text on the image
        g2d.drawString(text, x, y);

        // Dispose graphics
        g2d.dispose();
        String base64Image = "";

        // Encode the image to Base64
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            base64Image = Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "data:image/jpeg;base64," + base64Image;
    }

    private Map<String, Object> validateCredentials(User user, String password) {
        Map<String, Object> response = new HashMap<>();

        if (user == null) {
            response.put("status", "FAILURE");
            response.put("message", "User not found!");
        } else {
            UserCredential credential = userCredentialRepository.findByUser(user);

            if (credential == null) {
                credential = userCredentialRepository.save(
                        UserCredential.builder()
                                .user(user)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .createdBy(user.getId())
                                .updatedBy(user.getId())
                                .attempt(0)
                                .passwordHistory(user.getPassword())
                                .attemptedAt(LocalDateTime.now())
                                .isNew(1)
                                .lastLogin(LocalDateTime.now())
                                .build()
                );
            }

            long lastAttemptedElapsedTime = ChronoUnit.SECONDS.between(credential.getAttemptedAt() != null ? credential.getAttemptedAt() : LocalDateTime.now(), LocalDateTime.now());
            long timeToWait = 0;
            switch (credential.getAttempt()) {
                case 3:
                    timeToWait = 60;
                    break;
                case 6:
                    timeToWait = 180;
                    break;
                case 8:
                    timeToWait = 900;
                    break;
                case 12:
                    timeToWait = 1800;
                    break;
                case 15:
                    timeToWait = 3600;
                    break;
            }

            if (timeToWait == 0) {

                checkPassword(user, password, response, credential);
            } else {
                long remainingSeconds = timeToWait - lastAttemptedElapsedTime;

                System.out.println("Remaining seconds: " + remainingSeconds);
                if (remainingSeconds <= 0) {
                    checkPassword(user, password, response, credential);
                } else {
                    response.put("status", "FAILURE");
                    response.put("message", "Login attempt exceeded. please, retry after " + changeSecondsToMinute(remainingSeconds) + " minute(s)");
                }
            }
        }

        return response;
    }

    private void checkPassword(User user, String password, Map<String, Object> response, UserCredential credential) {

        if (BCrypt.checkpw(password, user.getPassword())) {
            credential.setAttempt(0);
            userCredentialRepository.save(credential);
            response.put("status", "SUCCESS");
            response.put("message", "Login successful!");
        } else {
            credential.setAttempt(Math.min(15, credential.getAttempt() + 1));
            credential.setAttemptedAt(LocalDateTime.now());
            userCredentialRepository.save(credential);
            response.put("status", "FAILURE");
            response.put("message", "Invalid Password");
        }
    }

    public static String changeSecondsToMinute(long seconds) {
        long minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    public Map<String, Object> passwordChangeRequest(String userId) {
        userId = getUserId(userId);

        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByEmail(userId).orElse(null);


        if (user != null) {
            PasswordReset previous = passwordResetRepository.findAll((root, query, criteriaBuilder) -> criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("requester"), user), criteriaBuilder.equal(root.get("status"), 0)), PageRequest.of(0, 1, Sort.by("id").descending())).getContent().stream().findFirst().orElse(null);
            String reference = UUID.randomUUID().toString();
            String otp = StringHelper.getRandomNumberString();
            PasswordReset passwordReset = PasswordReset.builder()
                    .reference(new BCryptPasswordEncoder().encode(reference))
                    .otp(new BCryptPasswordEncoder().encode(otp))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .otpExpiry(LocalDateTime.now().plusMinutes(10))
                    .status(0)
                    .requester(user)
                    .build();
            if (previous != null) {
                if (previous.getOtpExpiry().isAfter(LocalDateTime.now())) {
                    response.put("status", "FAILURE");
                    response.put("message", "Please, try again later.");

                    return response;
                }
                passwordReset = passwordResetRepository.save(passwordReset);
            } else {
                passwordReset = passwordResetRepository.save(passwordReset);
            }

            EmailDto email = EmailDto.builder()
                    .body("Your password reset otp is " + otp)
                    .subject("Password reset")
                    .receiver(user.getEmail())
                    .createdAt(LocalDateTime.now())
                    .createdBy(user.getId())
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(user.getId())
                    .build();

            JSONObject informationParameters = new JSONObject();
            informationParameters.put("otp", otp);
            informationParameters.put("name", user.getFirstName());
            JSONObject emailInformation = new JSONObject();
            emailInformation.put("parameters", informationParameters.toString());
            emailInformation.put("template", "otp-email");
            email.setInformation(emailInformation.toString());

            Map<String, Object> req = new HashMap<>();
            String emailReference = emailService.sendEmail(req);
            JSONObject information = new JSONObject();
            information.put("email_reference", emailReference);

            passwordReset.setInformation(information.toString());

            passwordResetRepository.save(passwordReset);

            response.put("status", "SUCCESS");
            response.put("message", "Password reset request has been sent successfully");
            response.put("reference", reference);
        } else {
            response.put("status", "FAILURE");
            response.put("message", "User not found!");
        }

        return response;
    }

    public Map<String, Object> verifyPasswordChange(ChangePasswordRequest request) {
        String userId = request.getEmail();
        userId = getUserId(userId);

        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByEmail(userId).orElse(null);


        if (user != null) {
            PasswordReset previous = passwordResetRepository.findAll((root, query, criteriaBuilder) -> criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("requester"), user), criteriaBuilder.equal(root.get("status"), 0)), PageRequest.of(0, 1, Sort.by("id").descending())).getContent().stream().findFirst().orElse(null);
            if (previous != null) {
                if (previous.getOtpExpiry().isBefore(LocalDateTime.now())) {
                    response.put("status", "FAILURE");
                    response.put("message", "Session expired.");
                    return response;
                }

                if (previous.getOtpAttempt() >= 3) {
                    response.put("status", "FAILURE");
                    response.put("message", "Please, try again later.");
                    return response;
                }

                if (BCrypt.checkpw(request.getOtp(), previous.getOtp()) && BCrypt.checkpw(request.getReference(), previous.getReference())) {

                    List<PasswordReset> all = passwordResetRepository.findAll((root, query, criteriaBuilder) -> criteriaBuilder.and(
                            criteriaBuilder.equal(root.get("requester"), user), criteriaBuilder.equal(root.get("status"), 1)));
                    for (PasswordReset passwordReset : all) {
                        passwordReset.setStatus(3); //revoked
                        passwordReset.setUpdatedAt(LocalDateTime.now());
                    }

                    previous.setStatus(1);
                    previous.setUpdatedAt(LocalDateTime.now());
                    response.put("status", "SUCCESS");
                    response.put("message", "Valid OTP");
                    passwordResetRepository.save(previous);
                } else {
                    previous.setOtpAttempt(previous.getOtpAttempt() + 1);
                    passwordResetRepository.save(previous);
                    response.put("status", "FAILURE");
                    response.put("message", "Invalid OTP!");
                }
            }
        } else {
            response.put("status", "FAILURE");
            response.put("message", "User not found!");
        }

        return response;
    }

    public Map<String, Object> resetPassword(ChangePasswordRequest request) {
        String userId = request.getEmail();
        userId = getUserId(userId);

        Map<String, Object> response = new HashMap<>();

        User user = userRepository.findByEmail(userId).orElse(null);


        if (user != null) {
            PasswordReset previous = passwordResetRepository.findAll((root, query, criteriaBuilder) -> criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("requester"), user), criteriaBuilder.equal(root.get("status"), 1)), PageRequest.of(0, 1, Sort.by("id").descending())).getContent().stream().findFirst().orElse(null);

            if (previous != null) {
                if (BCrypt.checkpw(request.getReference(), previous.getReference())) {
                    response = updateUser(User.builder().id(user.getId()).password(request.getNewPassword()).updatedAt(LocalDateTime.now()).updatedBy(user.getId()).status(user.getStatus()).build());
                    String status = Optional.ofNullable(response.get("status")).map(Object::toString).orElse("FAILURE");

                    if (status.equals("SUCCESS")) {
                        previous.setStatus(2);
                        passwordResetRepository.save(previous);
                    }
                }
            } else {
                response.put("status", "FAILURE");
                response.put("message", "Invalid request!");
            }
        } else {
            response.put("status", "FAILURE");
            response.put("message", "User not found!");
        }

        return response;
    }

    private String getUserId(String userId) {
        if (userId.startsWith("@")) {
            long id = Long.parseLong(userId.substring(3));
            userId = userRepository.findById(id).map(User::getEmail).orElse(userId);
        } else if (!userId.contains("@")) {
            userId = userRepository.findByPhoneNumber(userId).map(User::getEmail).orElse(userId);
        }
        return userId;
    }

    public Map<String, Object> updateUser(User user) {
        JSONObject responseJSON = new JSONObject();

        Optional<User> existingUser = userRepository.findById(user.getId());
        ArrayList<String> changedValues = new ArrayList<>();

        UserRole superAdminRole = userRoleRepository.findByAlias("super_admin");

        if (existingUser.isPresent()) {
            if (user.getFirstName() != null) {
                existingUser.get().setFirstName(user.getFirstName());
                changedValues.add("First name");
            }

            if (user.getLastName() != null) {
                changedValues.add("Last name");
                existingUser.get().setLastName(user.getLastName());
            }

            if (user.getPassword() != null) {
                changedValues.add("Password");
                String encryptedPassword = passwordEncoder.encode(user.getPassword());
                existingUser.get().setPassword(encryptedPassword);
            }

            if (user.getUserRole() != null) {
                changedValues.add("Role");
                existingUser.get().setUserRole(user.getUserRole());
            }

            if (user.getStatus() > -1) {
                changedValues.add("Status");
                existingUser.get().setStatus(user.getStatus());
            }

            if (changedValues.size() == 0) {
                responseJSON.put("status", "FAILURE");
                responseJSON.put("message", "There is nothing to change!");
                return responseJSON.toMap();
            }

            existingUser.get().setUpdatedAt(LocalDateTime.now());
            User savedUser = userRepository.save(existingUser.get());
            UserDto userDto = UserDto.builder()
                    .firstName(savedUser.getFirstName())
                    .lastName(savedUser.getLastName())
                    .id(savedUser.getId())
                    .email(savedUser.getEmail())
                    .information(new JSONObject(Optional.ofNullable(savedUser.getInformation()).map(Object::toString).orElse("{}")).toMap())
                    .status(savedUser.getStatus())
                    .role(savedUser.getUserRole())
                    .createdBy(savedUser.getCreatedBy())
                    .updatedBy(savedUser.getUpdatedBy())
                    .createdAt(savedUser.getCreatedAt())
                    .updatedAt(savedUser.getUpdatedAt()).build();
            responseJSON.put("status", "SUCCESS");
            responseJSON.put("user", userDto);

            String joined = String.join(", ", changedValues);

            responseJSON.put("message", String.format("Changed values: %s", joined));
        } else {
            responseJSON.put("status", "FAILURE");
            responseJSON.put("message", "User not found!");
        }

        return responseJSON.toMap();
    }


    public Map<String, Object> verifyEmail(String userEmail) {

        Map<String, Object> response = new HashMap<>();

        Optional<User> user = userRepository.findByEmail(userEmail);

        System.out.println("Response ");

        if(!user.isPresent()) {
            String reference = UUID.randomUUID().toString();
            String otp = StringHelper.getRandomNumberString();

            EmailVerify previous = emailVerifyRepository.findAll((root, query, criteriaBuilder) -> criteriaBuilder.and(
                    criteriaBuilder.equal(root.get("email"), userEmail), criteriaBuilder.equal(root.get("status"), 2)), PageRequest.of(0, 1, Sort.by("id").descending())).getContent().stream().findFirst().orElse(null);

            EmailVerify emailVerify = EmailVerify.builder()
                    .reference(new BCryptPasswordEncoder().encode(reference))
                    .otp(new BCryptPasswordEncoder().encode(otp))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .otpExpiry(LocalDateTime.now().plusMinutes(10))
                    .status(0)
                    .email(userEmail)
                    .build();
            if (previous != null) {
                if (previous.getOtpExpiry().isAfter(LocalDateTime.now())) {
                    response.put("status", "FAILURE");
                    response.put("message", "Please, try again later.");

                    return response;
                }
                emailVerify = emailVerifyRepository.save(emailVerify);
            } else {
                emailVerify = emailVerifyRepository.save(emailVerify);
            }
            EmailDto email = EmailDto.builder()
                    .body("Your password reset otp is " + otp)
                    .subject("Password reset")
                    .receiver(userEmail)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .createdBy(1)
                    .updatedBy(1)
                    .build();

            JSONObject informationParameters = new JSONObject();
            informationParameters.put("otp", otp);
            informationParameters.put("name", "");
            JSONObject emailInformation = new JSONObject();
            emailInformation.put("parameters", informationParameters.toString());
            emailInformation.put("template", "otp-email");
            email.setInformation(emailInformation.toString());

            Map<String, Object> req = new HashMap<>();
            req.put("information", emailInformation);
            req.put("body", "Your password reset otp is " + otp);
            req.put("subject", "Email verification");
            req.put("receiver", userEmail);

            String emailReference = emailService.sendEmail(req);
            JSONObject information = new JSONObject();
            information.put("email_reference", emailReference);

            emailVerify.setInformation(information.toString());

            emailVerifyRepository.save(emailVerify);

            response.put("status", "SUCCESS");
            response.put("message", "Password reset request has been sent successfully");
            response.put("reference", reference);

        }else {
            response.put("status", "FAILURE");
            response.put("message", "Email already exist, Please try with another one!");
        }

        return response;
    }

    public Map<String, Object> verifyEmailOtp(ChangePasswordRequest request) {

        Map<String, Object> response = new HashMap<>();

        EmailVerify previous = emailVerifyRepository.findAll((root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("email"), request.getEmail()), criteriaBuilder.equal(root.get("status"), 0)), PageRequest.of(0, 1, Sort.by("id").descending())).getContent().stream().findFirst().orElse(null);

        response.put("status", "FAILURE");
        response.put("message", "Something went wrong!");
        if (previous != null) {

            if (previous.getOtpExpiry().isBefore(LocalDateTime.now())) {
                response.put("status", "FAILURE");
                response.put("message", "Session expired.");
                return response;
            }

            if (previous.getOtpAttempt() >= 3) {
                response.put("status", "FAILURE");
                response.put("message", "Please, try again later.");
                return response;
            }


            if (BCrypt.checkpw(request.getReference(), previous.getReference()) && BCrypt.checkpw(request.getOtp(),previous.getOtp())) {
                previous.setStatus(2);
                EmailVerify save = emailVerifyRepository.save(previous);

                if (save.getId() != 0) {
                    response.put("status", "SUCCESS");
                    response.put("message", "Email verified successfully");
                }
            }
        }

        return response;

    }

    public Map<String, Object> verifyPhone(String phone) {

        Map<String, Object> response = new HashMap<>();

        Optional<User> user = userRepository.findByPhoneNumber(StringHelper.getCleanPhone(phone));

        if(!user.isPresent()) {
            response.put("status", "SUCCESS");
            response.put("message", "Good to go!");

        }else {
            response.put("status", "FAILURE");
            response.put("message", "Phone already exist, Please try with another one!");
        }


        return response;
    }

}
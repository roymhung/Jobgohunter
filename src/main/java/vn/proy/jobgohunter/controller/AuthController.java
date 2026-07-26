package vn.proy.jobgohunter.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import vn.proy.jobgohunter.domain.User;
import vn.proy.jobgohunter.domain.request.ReqChangePasswordDTO;
import vn.proy.jobgohunter.domain.request.ReqLoginDTO;
import vn.proy.jobgohunter.domain.response.ResCreateUserDTO;
import vn.proy.jobgohunter.domain.response.ResLoginDTO;
import vn.proy.jobgohunter.domain.response.ResUpdateUserDTO;
import vn.proy.jobgohunter.service.UserService;
import vn.proy.jobgohunter.util.SecurityUtil;
import vn.proy.jobgohunter.util.annotation.ApiMessage;
import vn.proy.jobgohunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jobgohunter.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;


    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder,
            SecurityUtil securityUtil, UserService userService, PasswordEncoder passwordEncoder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDTO) {

        // Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(),
                        loginDTO.getPassword());

        // xác thực người dùng => cần viết hàm loadUserByUsername
        Authentication authentication =
                authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // Create JWT token, set thong tin người dùng dang nhập vào SecurityContext(co the su dung
        // sau nay)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO res = new ResLoginDTO();
        User currentUserDB = this.userService.handleGetUserByUsername(loginDTO.getUsername());

        if (currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(currentUserDB.getId(),
                    currentUserDB.getEmail(), currentUserDB.getName(), currentUserDB.getRole());
            res.setUser(userLogin);
        }

        String access_Token = this.securityUtil.createAccessToken(authentication.getName(), res);

        res.setAccessToken(access_Token);

        // Create refresh token
        String refresh_token = this.securityUtil.createRefreshToken(loginDTO.getUsername(), res);

        // update user
        this.userService.updateUserToken(refresh_token, loginDTO.getUsername());

        // set cookies
        ResponseCookie resCookie = ResponseCookie.from("refresh_token", refresh_token)
                .httpOnly(true).secure(true).path("/").maxAge(refreshTokenExpiration).build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookie.toString()).body(res);
    }

    @GetMapping("/auth/account")
    @ApiMessage("fetch account")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        User currentUserDB = this.userService.handleGetUserByUsername(email);
        ResLoginDTO.UserAccountDetail userAccountDetail = new ResLoginDTO.UserAccountDetail();
        ResLoginDTO.UserGetAccount userGetAccount = new ResLoginDTO.UserGetAccount();

        if (currentUserDB != null) {
            userAccountDetail.setId(currentUserDB.getId());
            userAccountDetail.setEmail(currentUserDB.getEmail());
            userAccountDetail.setName(currentUserDB.getName());
            userAccountDetail.setAge(currentUserDB.getAge());
            userAccountDetail.setGender(currentUserDB.getGender());
            userAccountDetail.setAddress(currentUserDB.getAddress());
            userAccountDetail.setRole(currentUserDB.getRole());

            userGetAccount.setUser(userAccountDetail);
        }

        return ResponseEntity.ok().body(userGetAccount);
    }

    @PutMapping("/auth/account")
    @ApiMessage("Update current user account")
    public ResponseEntity<ResUpdateUserDTO> updateAccount(@RequestBody User reqUser)
            throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        if (email.equals("")) {
            throw new IdInvalidException("Access token is Invalid");
        }

        User currentUserDB = this.userService.handleGetUserByUsername(email);
        if (currentUserDB == null) {
            throw new IdInvalidException("User not found");
        }

        reqUser.setId(currentUserDB.getId());
        User updatedUser = this.userService.handleUpdateUser(reqUser);
        return ResponseEntity.ok(this.userService.convertToResUpdateUserDTO(updatedUser));
    }

    @PostMapping("/auth/change-password")
    @ApiMessage("Change current user password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ReqChangePasswordDTO dto)
            throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        if (email.equals("")) {
            throw new IdInvalidException("Access token is Invalid");
        }

        User currentUserDB = this.userService.handleGetUserByUsername(email);
        if (currentUserDB == null) {
            throw new IdInvalidException("User not found");
        }

        if (!this.passwordEncoder.matches(dto.getCurrentPassword(), currentUserDB.getPassword())) {
            throw new IdInvalidException("Mật khẩu hiện tại không đúng");
        }

        currentUserDB.setPassword(this.passwordEncoder.encode(dto.getNewPassword()));
        this.userService.saveUser(currentUserDB);

        return ResponseEntity.ok().body(null);
    }


    @GetMapping("/auth/refresh")
    @ApiMessage("Get new access token by refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "abc") String refresh_token)
            throws IdInvalidException {

        if (refresh_token.equals("abc")) {
            throw new IdInvalidException("Refresh token is missing");
        }

        // check valid refresh token
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refresh_token);

        String email = decodedToken.getSubject();

        // check user bu token + email
        User currentUser = this.userService.getUserByRefreshTokenAndEmail(refresh_token, email);
        if (currentUser == null) {
            throw new IdInvalidException("Refresh token is invalid");
        }

        // issue new token/set refresh token as cookies

        ResLoginDTO res = new ResLoginDTO();
        User currentUserDB = this.userService.handleGetUserByUsername(email);

        if (currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(currentUserDB.getId(),
                    currentUserDB.getEmail(), currentUserDB.getName(), currentUserDB.getRole());
            res.setUser(userLogin);
        }

        String access_Token = this.securityUtil.createAccessToken(email, res);

        res.setAccessToken(access_Token);

        // Create refresh token
        String new_refresh_token = this.securityUtil.createRefreshToken(email, res);

        // update user
        this.userService.updateUserToken(new_refresh_token, email);

        // set cookies
        ResponseCookie resCookie = ResponseCookie.from("refresh_token", new_refresh_token)
                .httpOnly(true).secure(true).path("/").maxAge(refreshTokenExpiration).build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookie.toString()).body(res);
    }


    @PostMapping("/auth/logout")
    @ApiMessage("Logout User")
    public ResponseEntity<Void> logout() throws IdInvalidException {

        String email = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get()
                : "";

        if (email.equals("")) {
            throw new IdInvalidException("Access token is Invalid");
        }

        // update refresh token = null
        this.userService.updateUserToken(null, email);

        // remove refresh token in cookies
        ResponseCookie deleteSpringCookie = ResponseCookie.from("refresh_token", null)
                .httpOnly(true).secure(true).path("/").maxAge(0).build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                .body(null);
    }

    @PostMapping("/auth/register")
    @ApiMessage("Register a new user")
    public ResponseEntity<ResCreateUserDTO> register(@Valid @RequestBody User postManUser)
            throws IdInvalidException {
        boolean isEmailExist = this.userService.isEmailExist(postManUser.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException(
                    "Email " + postManUser.getEmail() + " đã tồn tại, vui lòng sử dụng email khác");
        }

        String hashPassword = this.passwordEncoder.encode(postManUser.getPassword());
        postManUser.setPassword(hashPassword);
        User regisUser = this.userService.handleCreateUser(postManUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.userService.convertToResCreateUserDTO(regisUser));
    }
}

package com.wby.community.controller;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.wby.community.annotation.LoginRequired;
import com.wby.community.entity.Comment;
import com.wby.community.entity.DiscussPost;
import com.wby.community.entity.Page;
import com.wby.community.entity.User;
import com.wby.community.service.*;
import com.wby.community.util.CommunityConstant;
import com.wby.community.util.CommunityUtil;
import com.wby.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @LoginRequired
    @RequestMapping(path = "setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        // ??????????????????
        String fileName = CommunityUtil.generateUUID();
        // ??????????????????
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // ??????????????????
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "/site/setting";
    }

    // ??????????????????
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "?????????????????????");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }

    // ??????
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "???????????????????????????");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "???????????????????????????");
            return "/site/setting";
        }

        // ????????????????????????
        filename = CommunityUtil.generateUUID() + suffix;
        // ???????????????????????????
        File dest = new File(uploadPath + "/" + filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("?????????????????????" + e.getMessage());
            throw new RuntimeException("?????????????????????????????????????????????", e);
        }

        // ???????????????????????????????????????web???????????????
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);
        return "redirect:/index";

    }

    // ??????
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // ????????????????????????
        fileName = uploadPath + "/" + fileName;
        // ???????????????
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // ????????????
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("?????????????????????" + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(String originalPassword, String newPassword, String confirmPassword,
                                 @CookieValue("ticket") String ticket, Model model) {
        if (originalPassword == null) {
            model.addAttribute("originalPasswordMsg", "????????????????????????");
            return "/site/setting";
        }

        if (newPassword == null) {
            model.addAttribute("newPasswordMsg", "?????????????????????");
            return "/site/setting";
        }

        if (confirmPassword == null) {
            model.addAttribute("confirmPasswordMsg", "????????????????????????");
            return "/site/setting";
        }

        User user = hostHolder.getUser();
        if (!CommunityUtil.md5(originalPassword + user.getSalt()).equals(user.getPassword())) {
            model.addAttribute("originalPasswordMsg", "???????????????");
            return "/site/setting";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("confirmPasswordMsg", "??????????????????????????????");
            return "/site/setting";
        }

        userService.updatePassword(user.getId(), newPassword);
        userService.logout(ticket);
        return "redirect:/login";


    }

    // ????????????
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("?????????????????????");
        }

        // ??????
        model.addAttribute("user", user);
        // ????????????
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // ????????????
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        // ????????????
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        // ???????????????
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }

    @RequestMapping(path = "/userPost/{userId}", method = RequestMethod.GET)
    public String getUserDiscussPostList(@PathVariable("userId") int userId, Model model, Page page) {

        page.setPath("/user/userPost/" + userId);
        page.setRows(discussPostService.findDiscussPostRows(userId));

        // ?????????????????????????????????
        List<DiscussPost> discussPosts =
                discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(), 0);

        List<Map<String, Object>> discussPostsMap = new ArrayList<>();
        for (DiscussPost discussPost : discussPosts) {
            Map<String, Object> map = new HashMap<>();
            map.put("discussPost", discussPost);
            map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId()));

            discussPostsMap.add(map);
        }

        User targetUser = userService.findUserById(userId);
        model.addAttribute("user", targetUser);

        model.addAttribute("discussPosts", discussPostsMap);

        return "/site/user-post";
    }

    @RequestMapping(path = "/userComment/{userId}", method = RequestMethod.GET)
    public String getUserCommentList(@PathVariable("userId") int userId, Model model, Page page) {

        page.setPath("/user/userComment/" + userId);
        page.setRows(commentService.findCommentCountByUserIdAndEntityType(
                userId, CommunityConstant.ENTITY_TYPE_POST));

        List<Comment> comments =
                commentService.findCommentByUserIdAndEntityType(
                        userId, ENTITY_TYPE_POST, page.getOffset(), page.getLimit());

        List<Map<String, Object>> commentsMap = new ArrayList<>();
        for (Comment comment : comments) {
            Map<String, Object> map = new HashMap<>();
            map.put("comment", comment);
            map.put("postOfComment", discussPostService.findDiscussPostById(comment.getEntityId()));
            commentsMap.add(map);
        }

        model.addAttribute("user", userService.findUserById(userId));
        model.addAttribute("comments", commentsMap);

        return "/site/user-comment";
    }
}

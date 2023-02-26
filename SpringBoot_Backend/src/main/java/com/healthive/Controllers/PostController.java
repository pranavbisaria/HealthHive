package com.healthive.Controllers;

import com.healthive.Models.Comments;
import com.healthive.Models.User;
import com.healthive.Payloads.HealthRecordDto;
import com.healthive.Payloads.PageableDto;
import com.healthive.Payloads.PostDto;
import com.healthive.Security.CurrentUser;
import com.healthive.Service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path ="/api/posts")
@RequiredArgsConstructor()
public class PostController {
    private final PostService postService;
    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PostMapping("/createPost")
    public ResponseEntity<?> createPost(@CurrentUser User user, @RequestParam(value ="categoryId", defaultValue = "0", required = false) Integer categoryId, @RequestPart(value = "images", required = false) MultipartFile[] images, @Valid @RequestPart("description") String description){
        PostDto postDto = new PostDto();
        postDto.setDescription(description);
        return this.postService.createPost(categoryId, user, postDto, images);
    }
    @GetMapping("/getAllPosts")
    public ResponseEntity<?> getAllPosts(@CurrentUser User user,
                                         @RequestParam(value ="pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                         @RequestParam(value ="pageSize", defaultValue = "5", required = false) Integer pageSize
    ){
        return new ResponseEntity<>(this.postService.getAllPost(user, new PageableDto(pageNumber, pageSize, null, null)), OK);
    }
    @GetMapping("/getPostById/{postId}")
    public ResponseEntity<?> getPostID(@PathVariable("postId") Long postId, @CurrentUser User user){
        return this.postService.getPostByID(user, postId);
    }
    @DeleteMapping("/deletePost/{postId}")
    public ResponseEntity<?> deletePost(@CurrentUser User user, @PathVariable("postId") Long postId){
        return this.postService.deletePost(postId, user);
    }
    @PatchMapping("/updatePost/{postId}")
    public ResponseEntity<?> updatePost(@CurrentUser User user, @PathVariable("postId") Long postId, @Valid @RequestPart("description") String description){
        PostDto postDto = new PostDto();
        postDto.setDescription(description);
        return this.postService.updatePost(postId, user, postDto);
    }
    @PatchMapping("/updatePostImage/{postId}")
    public ResponseEntity<?> updatePostImage(@CurrentUser User user, @PathVariable("postId") Long postId,  @RequestPart(value = "images", required = false) MultipartFile[] images){
        return this.postService.updatePostImage(postId, user, images);
    }

// ------------------------------------------------------------------ Comments ------------------------------------------------------------------
    @PostMapping("/addComment/{postId}")
    public ResponseEntity<?> addComment(@CurrentUser User user, @PathVariable("postId") Long postId, @RequestBody Comments comments){
        return this.postService.addAComment(user, postId, comments);
    }
    @GetMapping("/getAllComments/{postId}")
    public ResponseEntity<?> getAllComments(@CurrentUser User user, @PathVariable("postId") Long postId,
                                         @RequestParam(value ="pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                         @RequestParam(value ="pageSize", defaultValue = "5", required = false) Integer pageSize
    ){
        return new ResponseEntity<>(this.postService.getAllComments(user, postId, new PageableDto(pageNumber, pageSize, null, null)), OK);
    }
    @DeleteMapping("/deleteComment/{commentId}")
    public ResponseEntity<?> deleteAComment(@CurrentUser User user, @PathVariable("commentId") Long commentId){
        return this.postService.deleteComment(user, commentId);
    }
}

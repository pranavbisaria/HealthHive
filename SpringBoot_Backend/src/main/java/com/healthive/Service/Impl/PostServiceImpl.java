package com.healthive.Service.Impl;
import com.healthive.Exceptions.ResourceNotFoundException;
import com.healthive.Models.*;
import com.healthive.Payloads.*;
import com.healthive.Repository.*;
import com.healthive.Service.PostService;
import com.healthive.Service.StorageServices;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService{
    private final ModelMapper modelMapper;
    private final CategoryRepo categoryRepo;
    private final PostRepo postRepo;
    private final CommentRepo commentRepo;
    private final StorageServices storageServices;
    private final RoleRepo roleRepo;
    private final UserRepo userRepo;
    @Override
    public ResponseEntity<?> createPost(Integer categoryId, User user, PostDto postDto, MultipartFile[] images){
        if (FileValidation(images))
            return new ResponseEntity<>(new ApiResponse("File is not of image type(JPEG/ JPG or PNG)!!!", false), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        Arrays.stream(images).forEach(multipartFile -> {
            Images images1 = new Images();
            images1.setImageUrl(this.storageServices.uploadFile(multipartFile));
            postDto.getPostImage().add(images1);
        });
        Category category = this.categoryRepo.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "CategoryId", categoryId));
        postDto.setCategory(category);
        Post post = this.modelMapper.map(postDto, Post.class);
        post.setUser(user);
        this.postRepo.save(post);
        return new ResponseEntity<>(new ApiResponse("Post has been Added", true), HttpStatus.CREATED);
    }
    @Override
    public ResponseEntity<?> updatePost(Long postId, User user, PostDto postDto){
        Post post = this.postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        if(!Objects.equals(user, post.getUser())) return new ResponseEntity<>(new ApiResponse("User is not authorize", false), HttpStatus.FORBIDDEN);
        post.setDescription(postDto.getDescription());
        postDto.setPostedOn(timeString(post.getPostedOn()));
        if(user.getLikedPost().contains(post)) postDto.setIfLiked(true);
        return new ResponseEntity<>(postDto, HttpStatus.OK);
    }
    @Override
    public ResponseEntity<?> updatePostImage(Long postId, User user, MultipartFile[] images){
        Post post = this.postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        if(!Objects.equals(user, post.getUser())) return new ResponseEntity<>(new ApiResponse("User is not authorize", false), HttpStatus.FORBIDDEN);
        if (FileValidation(images))
            return new ResponseEntity<>(new ApiResponse("File is not of image type(JPEG/ JPG or PNG)!!!", false), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        post.getPostImage().clear();
        Arrays.stream(images).forEach(multipartFile -> {
            Images images1 = new Images();
            images1.setImageUrl(this.storageServices.uploadFile(multipartFile));
            post.getPostImage().add(images1);
        });
        PostDto postDto = this.modelMapper.map(post, PostDto.class);
        postDto.setPostedOn(timeString(post.getPostedOn()));
        if(user.getLikedPost().contains(post)) postDto.setIfLiked(true);
        return new ResponseEntity<>(postDto, HttpStatus.OK);
    }
    @Override
    public ResponseEntity<?> deletePost(Long postId, User user){
        Post post = this.postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        if(!Objects.equals(user, post.getUser())) return new ResponseEntity<>(new ApiResponse("User is not authorize", false), HttpStatus.FORBIDDEN);
        this.postRepo.delete(post);
        return new ResponseEntity<>(new ApiResponse("Post has been successfully deleted", true), HttpStatus.OK);
    }
    @Override
    public PageResponse getAllPost(User user, PageableDto pageable){
        Integer pN = pageable.getPageNumber(), pS = pageable.getPageSize();
        Pageable p = PageRequest.of(pN, pS);
        Page<Post> pagePost = this.postRepo.findAll(p);
        List<Post> allProducts = pagePost.getContent();
        List<PostDto> postDTO = new ArrayList<>();
        for (Post loopPost : allProducts) {
            PostDto loopPostDTO = this.modelMapper.map(loopPost, PostDto.class);
            loopPostDTO.setPostedOn(timeString(loopPost.getPostedOn()));
            if(user!=null){ if(user.getLikedPost().contains(loopPost)) loopPostDTO.setIfLiked(true);}
            postDTO.add(loopPostDTO);
        }
        return new PageResponse(new ArrayList<>(postDTO),pagePost.getNumber(), pagePost.getSize(), pagePost.getTotalPages(), pagePost.getTotalElements(), pagePost.isLast());
    }
    @Override
    public ResponseEntity<?> getPostByID(User user, Long postId){
        Post post = this.postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        PostDto postDto = this.modelMapper.map(post, PostDto.class);
        postDto.setPostedOn(timeString(post.getPostedOn()));
        if(user!=null){ if(user.getLikedPost().contains(post)) postDto.setIfLiked(true);}
        return new ResponseEntity<>(postDto, HttpStatus.OK);
    }
    private String timeString(Date time){
        long difference  = (time.getTime() - System.currentTimeMillis());
        long diffInSeconds = difference / 1000;
        long diffInMinutes = diffInSeconds / 60;
        long diffInHours = diffInMinutes / 60;
        long diffInDays = diffInHours / 24;

        if (diffInSeconds < 60) {
            return diffInSeconds + " seconds ago";
        } else if (diffInMinutes < 60) {
            return diffInMinutes + " minutes ago";
        } else if (diffInHours < 24) {
            return diffInHours + " hours ago";
        } else if (diffInDays < 7) {
            return diffInDays + " days ago";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
            return formatter.format(time);
        }
    }
    private boolean FileValidation(MultipartFile[] images) throws NullPointerException{
        for (MultipartFile image : images) {
            if (!image.getContentType().equals("image/png") && !image.getContentType().equals("image/jpg") && !image.getContentType().equals("image/jpeg") && !image.getContentType().equals("image/webp")) {
                return true;
            }
        }
        return false;
    }

// --------------------------------------------------- Comment ---------------------------------------------------------------------------
    @Override
    public ResponseEntity<?> addAComment(User user, Long postId, Comments comments){
        Post post = this.postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        post.setCommentCount(post.getCommentCount()+1L);
        post.getComments().add(comments);
        comments.setPost(post);
        comments.setUser(user);
        this.commentRepo.save(comments);
        return new ResponseEntity<>(new ApiResponse("Comment has been added", true), HttpStatus.OK);
    }
    @Override
    public PageResponse getAllComments(User user, Long postId, PageableDto pageable){
        Post post = this.postRepo.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "postId", postId));
        Integer pN = pageable.getPageNumber(), pS = pageable.getPageSize();
        Pageable p = PageRequest.of(pN, pS);
        Page<Comments> pageComment = this.commentRepo.findAllByPost(post, p);;
        List<Comments> allComment = pageComment.getContent();
        List<CommentsDto> commentDTO = new ArrayList<>();
        for (Comments loopComment : allComment) {
            CommentsDto loopCommentDTO = this.modelMapper.map(loopComment, CommentsDto.class);
            loopCommentDTO.setPostedOn(timeString(loopComment.getPostedOn()));
            if(user!=null){ if(user.getLikedComments().contains(loopComment)) loopCommentDTO.setIfLiked(true);}
            commentDTO.add(loopCommentDTO);
        }
        return new PageResponse(new ArrayList<>(commentDTO),pageComment.getNumber(), pageComment.getSize(), pageComment.getTotalPages(), pageComment.getTotalElements(), pageComment.isLast());
    }
    @Override
    public ResponseEntity<?> deleteComment(User user, Long commentId){
        Comments comments = this.commentRepo.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment", "commentId:", commentId));
        if(!Objects.equals(user, comments.getUser())) return new ResponseEntity<>(new ApiResponse("User is not authorize", false), HttpStatus.FORBIDDEN);
        this.commentRepo.delete(comments);
        return new ResponseEntity<>(new ApiResponse("Comment has been successfully deleted", true), HttpStatus.OK);
    }
}

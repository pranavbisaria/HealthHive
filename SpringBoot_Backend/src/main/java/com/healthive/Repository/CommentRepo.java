package com.healthive.Repository;
import com.healthive.Models.Comments;
import com.healthive.Models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepo extends JpaRepository<Comments, Long> {
    Page<Comments> findAllByPost(Post post, Pageable pageable);
}

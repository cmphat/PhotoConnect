package com.photoconnect.service;

import com.photoconnect.dto.FeedCreatorDto;
import com.photoconnect.dto.FeedPostDto;

import java.util.List;

public interface FeedService {
    List<FeedPostDto> getFeedPosts(String category, Long currentUserId, int limit);
    List<FeedPostDto> getFeedPosts(String category, String authorType, Long currentUserId, boolean isPhotographer, int limit);
    List<FeedCreatorDto> getFeaturedCreators();
}

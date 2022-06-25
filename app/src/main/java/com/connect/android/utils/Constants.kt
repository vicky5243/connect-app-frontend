package com.connect.android.utils

class Constants {
    companion object {
        private const val URL = "http://10.0.2.2:3000/"
        const val BASE_URL = URL + "api/"

        // Assets Url
        const val PROFILE_IMAGE_URL = URL + "images/profile"
        const val POST_IMAGE_URL = URL + "images/posts"

        /**
         * END POINTS
         */
        // Authentication
        const val SEND_EMAIL = "users/auth/email/sendconfirmationcode"
        const val VERIFY_EMAIL = "users/auth/email/verifyconfirmationcode"
        const val SIGN_UP = "users/auth/signup"
        const val SIGN_IN = "users/auth/signin"
        const val LOG_OUT = "users/auth/logout"
        const val NEW_TOKENS = "users/auth/refresh-token"

        // Posts
        const val GET_NEWSFEED_POSTS = "posts"
        const val CREATE_POST = "posts"
        const val LIKE_OR_UNLIKE_A_POST = "posts/likes/{pid}"
        const val LIKES_OF_A_POST = "posts/likes"
        const val COMMENTS_OF_A_POST = "posts/comments"
        const val COMMENT_ON_A_POST = "posts/comments/{pid}"

        // Profile
        const val CURRENT_USER = "users/currentUser"
        const val GET_USER_BY_ID = "users"
        const val SEARCH_USER = "users/search"
        const val CHANGE_PASSWORD = "users/accounts/changepassword"
        const val EDIT_PROFILE = "users/accounts/editProfile"
        const val FOLLOW_OR_UNFOLLOW = "users/account/follows/{uid}"
    }
}
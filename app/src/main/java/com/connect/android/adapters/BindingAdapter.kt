package com.connect.android.adapters

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.format.DateUtils
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.connect.android.R
import com.connect.android.models.res.Comment
import com.connect.android.models.res.NewsfeedPost
import com.connect.android.models.res.SearchUser
import com.connect.android.utils.Constants
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Search users
 */
@BindingAdapter("userProfilePhoto")
fun setUserProfilePhoto(imgView: ImageView, searchUser: SearchUser?) {
    searchUser?.let {
        Glide.with(imgView.context)
            .load("${Constants.PROFILE_IMAGE_URL}/${it.profilePhotoUrl}")
            .into(imgView)
    }
}

@BindingAdapter("userUsername")
fun setUserUsername(textView: TextView, searchUser: SearchUser?) {
    searchUser?.let {
        textView.text = it.username
    }
}

@BindingAdapter("userFullname")
fun setUserFullname(textView: TextView, searchUser: SearchUser?) {
    searchUser?.let {
        if (it.fullname == null) {
            textView.visibility = View.GONE
        } else {
            textView.text = it.fullname
        }
    }
}

/**
 * Liked by users
 */
@BindingAdapter("likedByProfilePhoto")
fun setLikedByProfilePhoto(imgView: ImageView, searchUser: SearchUser?) {
    searchUser?.let {
        Glide.with(imgView.context)
            .load("${Constants.PROFILE_IMAGE_URL}/${it.profilePhotoUrl}")
            .into(imgView)
    }
}

@BindingAdapter("likedByUsername")
fun setLikedByUsername(textView: TextView, searchUser: SearchUser?) {
    searchUser?.let {
        textView.text = it.username
    }
}

@BindingAdapter("likedByFullname")
fun setLikedByFullname(textView: TextView, searchUser: SearchUser?) {
    searchUser?.let {
        if (it.fullname == null) {
            textView.visibility = View.GONE
        } else {
            textView.text = it.fullname
        }
    }
}

/**
 * Commented by users
 */
@BindingAdapter("commentedByProfilePhoto")
fun setCommentedByProfilePhoto(imgView: ImageView, searchUser: SearchUser?) {
    searchUser?.let {
        Glide.with(imgView.context)
            .load("${Constants.PROFILE_IMAGE_URL}/${it.profilePhotoUrl}")
            .into(imgView)
    }
}

@BindingAdapter("commentedByCommentText")
fun setCommentedByCommentText(textView: TextView, comment: Comment?) {
    comment?.let {
        val usernameSpannableString = SpannableString(it.commentedBy.username)
        val start = 0
        val end = usernameSpannableString.length
        usernameSpannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textView.text = usernameSpannableString
        val commentSpannableString = SpannableString(it.commentText)
        textView.append(" $commentSpannableString")
    }
}

@BindingAdapter("commentedByDate")
fun setCommentedByDate(textView: TextView, comment: Comment?) {
    comment?.let {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        try {
            val time = sdf.parse(it.commentCreatedAt)?.time
            val now = System.currentTimeMillis()
            val ago = time?.let { it1 ->
                DateUtils.getRelativeTimeSpanString(
                    it1,
                    now,
                    DateUtils.MINUTE_IN_MILLIS
                )
            }
            textView.text = ago
        } catch (e: ParseException) {
            Timber.d("Exception: ${e.message}")
        }
    }
}

/**
 * Newsfeed posts
 * Nf means Newsfeed
 */
@BindingAdapter("nfPostCreatedByProfilePhoto")
fun setNfPostCreatedByProfilePhoto(imgView: ImageView, newsfeedPost: NewsfeedPost?) {
    newsfeedPost?.let {
        Glide.with(imgView.context)
            .load("${Constants.PROFILE_IMAGE_URL}/${it.postCreatedByUser.postCreatedByProfilePhotoUrl}")
            .into(imgView)
    }
}

@BindingAdapter("nfPostCreatedByUsername")
fun setNfPostCreatedByUsername(textView: TextView, newsfeedPost: NewsfeedPost?) {
    newsfeedPost?.let {
        textView.text = it.postCreatedByUser.postCreatedByUsername
    }
}

@BindingAdapter("nfPostTitle")
fun setNfPostTitle(textView: TextView, newsfeedPost: NewsfeedPost?) {
    newsfeedPost?.let {
        if (it.postTitle == null) {
            textView.visibility = View.GONE
        } else {
            textView.text = it.postTitle
        }
    }
}

@BindingAdapter("nfPostImage")
fun setNfPostImage(imgView: ImageView, newsfeedPost: NewsfeedPost?) {
    newsfeedPost?.let {
        Glide.with(imgView.context)
            .load("${Constants.POST_IMAGE_URL}/${it.postImageUrl}")
            .into(imgView)
    }
}

@BindingAdapter("nfPostHasLiked")
fun setNFPostHasLiked(imgView: ImageView, newsfeedPost: NewsfeedPost?) {
    newsfeedPost?.let {
        if (newsfeedPost.hasLiked) {
            imgView.setImageResource(R.drawable.ic_like)
        } else {
            imgView.setImageResource(R.drawable.ic_unlike)
        }
    }
}

@BindingAdapter("nfPostNumLikes")
fun setNfPostNumLikes(textView: TextView, newsfeedPost: NewsfeedPost?) {
    newsfeedPost?.let {
        if (it.numLikes <= 1) textView.text = "${it.numLikes} Like"
        else textView.text = it.numLikes.toString() + " Likes"

    }
}

@BindingAdapter("nfPostNumComments")
fun setNfPostNumComments(textView: TextView, newsfeedPost: NewsfeedPost?) {
    newsfeedPost?.let {
        if (it.numComments <= 1) textView.text = "${it.numComments} Comment"
        else textView.text = "View all ${it.numComments} Comments"
    }
}

@BindingAdapter("nfPostDesc")
fun setNfPostDesc(textView: TextView, newsfeedPost: NewsfeedPost?) {
    newsfeedPost?.let {
        if (it.postDescription.isNullOrEmpty()) {
            textView.visibility = View.GONE
        } else {
            textView.text = it.postDescription
        }
    }
}

@BindingAdapter("nfPostDate")
fun setNfPostDate(textView: TextView, newsfeedPost: NewsfeedPost?) {
    newsfeedPost?.let {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        try {
            val time = sdf.parse(it.postCreatedAt)?.time
            val now = System.currentTimeMillis()
            val ago = time?.let { it1 ->
                DateUtils.getRelativeTimeSpanString(
                    it1,
                    now,
                    DateUtils.MINUTE_IN_MILLIS
                )
            }
            textView.text = ago
        } catch (e: ParseException) {
            Timber.d("Exception: ${e.message}")
        }
    }
}
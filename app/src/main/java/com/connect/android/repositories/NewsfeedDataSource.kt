package com.connect.android.repositories

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.connect.android.models.req.NewTokensReq
import com.connect.android.models.res.NewsfeedPost
import com.connect.android.network.Api
import com.connect.android.utils.RetrofitError
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

const val NEWS_FEED_POSTS_PAGE_INDEX = 1

class NewsfeedDataSource(
    private val api: Api,
    private val authRepository: AuthRepository,
    private val uid: Long?
) :
    PagingSource<Int, NewsfeedPost>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NewsfeedPost> {
        Timber.d("load")
        val currentLoadingPageKey = params.key ?: NEWS_FEED_POSTS_PAGE_INDEX
        val prevKey =
            if (currentLoadingPageKey == NEWS_FEED_POSTS_PAGE_INDEX) null else currentLoadingPageKey - 1

        val accessToken = authRepository.getAccessToken()
        accessToken?.let { token ->
            Timber.d("accessToken $accessToken")
            try {
                val response = if (uid == null) {
                    api.getNewsfeedPosts("Bearer $token", currentLoadingPageKey)
                } else {
                    api.getPostsOfAnUser("Bearer $token", uid, currentLoadingPageKey)
                }
                Timber.d("response: $response")
                when {
                    response.isSuccessful -> {
                        val posts = response.body()?.data?.posts ?: emptyList()
                        Timber.d("posts: $posts")
                        val nextKey =
                            if (currentLoadingPageKey < response.body()?.data?.totalPages!!) currentLoadingPageKey + 1 else null

                        return LoadResult.Page(
                            data = posts,
                            prevKey = prevKey,
                            nextKey = nextKey
                        )
                    }
                    response.code() == 404 -> {
                        return LoadResult.Error(Throwable("404"))
                    }
                    else -> {
                        Timber.d("Unsuccessful: $response")
                        val errorRes = RetrofitError.convertErrorBody(response.errorBody())
                        errorRes?.let {
                            if (it.error.message == "Token Expired" && it.error.code == 401) {
                                Timber.d("TOKEN EXPIRED")
                                val refreshToken = authRepository.getRefreshToken()
                                refreshToken?.let { _refreshToken ->
                                    Timber.d("_refreshToken $_refreshToken")
                                    //Todo It could be refresh token invalid cause, if user logged in from another device
                                    val newToken =
                                        authRepository.getNewTokens(NewTokensReq(_refreshToken))
                                    newToken?.let { _token ->
                                        Timber.d("_token $_token")
                                        val res = if (uid == null) {
                                            api.getNewsfeedPosts("Bearer $_token", currentLoadingPageKey)
                                        } else {
                                            api.getPostsOfAnUser("Bearer $_token", uid, currentLoadingPageKey)
                                        }
                                        if (res.isSuccessful) {
                                            val posts = res.body()?.data?.posts ?: emptyList()
                                            Timber.d("posts: $posts")
                                            val nextKey =
                                                if (currentLoadingPageKey < res.body()?.data?.totalPages!!) currentLoadingPageKey + 1 else null

                                            return LoadResult.Page(
                                                data = posts,
                                                prevKey = prevKey,
                                                nextKey = nextKey
                                            )
                                        } else if (res.code() == 404) {
                                            return LoadResult.Error(Throwable("404"))
                                        }
                                    }
                                }
                            } else {
                                Timber.d("errorRes: ${it.error.message}")
                                return LoadResult.Error(Throwable(it.error.message))
                            }
                        }
                        return LoadResult.Error(Throwable("500"))
                    }
                }
            } catch (exception: IOException) {
                Timber.d("NewsfeedDataSource: exception ${exception.message}")
                return LoadResult.Error(Throwable("No Internet"))
            } catch (exception: HttpException) {
                Timber.d("NewsfeedDataSource: exception ${exception.message}")
                return LoadResult.Error(Throwable("500"))
            }
        }
        return LoadResult.Error(Throwable("500"))
    }

    override fun getRefreshKey(state: PagingState<Int, NewsfeedPost>): Int? {
        Timber.d("getRefreshKey")
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
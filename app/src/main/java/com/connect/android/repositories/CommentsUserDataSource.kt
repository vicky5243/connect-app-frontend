package com.connect.android.repositories

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.connect.android.models.req.NewTokensReq
import com.connect.android.models.res.Comment
import com.connect.android.network.Api
import com.connect.android.utils.RetrofitError
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

const val Comment_USERS_PAGE_INDEX = 1

class CommentsUserDataSource(
    private val api: Api,
    private val pid: Long,
    private val authRepository: AuthRepository
) : PagingSource<Int, Comment>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Comment> {
        val currentLoadingPageKey = params.key ?: Comment_USERS_PAGE_INDEX
        val prevKey =
            if (currentLoadingPageKey == Comment_USERS_PAGE_INDEX) null else currentLoadingPageKey - 1

        val accessToken = authRepository.getAccessToken()
        accessToken?.let { token ->
            try {
                val response = api.getCommentsOfAPost("Bearer $token", currentLoadingPageKey, pid)
                when {
                    response.isSuccessful -> {
                        val comments = response.body()?.data?.comments ?: emptyList()
                        Timber.d("comments $comments")
                        val nextKey =
                            if (currentLoadingPageKey < response.body()?.data?.totalPages!!) currentLoadingPageKey + 1 else null

                        return LoadResult.Page(
                            data = comments,
                            prevKey = prevKey,
                            nextKey = nextKey,
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
                                val refreshToken = authRepository.getRefreshToken()
                                refreshToken?.let { _refreshToken ->
                                    val newToken =
                                        authRepository.getNewTokens(NewTokensReq(_refreshToken))
                                    newToken?.let { _token ->
                                        val res =
                                            api.getCommentsOfAPost(
                                                "Bearer $_token",
                                                currentLoadingPageKey,
                                                pid
                                            )
                                        if (res.isSuccessful) {
                                            val comments = res.body()?.data?.comments ?: emptyList()

                                            val nextKey =
                                                if (currentLoadingPageKey < res.body()?.data?.totalPages!!) currentLoadingPageKey + 1 else null

                                            return LoadResult.Page(
                                                data = comments,
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

    override fun getRefreshKey(state: PagingState<Int, Comment>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
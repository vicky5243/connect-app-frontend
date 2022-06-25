package com.connect.android.repositories

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.connect.android.models.req.NewTokensReq
import com.connect.android.models.res.SearchUser
import com.connect.android.network.Api
import com.connect.android.utils.RetrofitError
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

const val SEARCH_USERS_PAGE_INDEX = 1

class SearchUsersDataSource(
    private val api: Api,
    private val query: String,
    private val authRepository: AuthRepository
) :
    PagingSource<Int, SearchUser>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchUser> {
        Timber.d("load")
        if (query.isNotBlank()) {
            Timber.d("query: $query")
            val currentLoadingPageKey = params.key ?: SEARCH_USERS_PAGE_INDEX
            val prevKey =
                if (currentLoadingPageKey == SEARCH_USERS_PAGE_INDEX) null else currentLoadingPageKey - 1

            val accessToken = authRepository.getAccessToken()
            accessToken?.let { token ->
                try {
                    val response = api.searchUser("Bearer $token", currentLoadingPageKey, query)
                    Timber.d("response $response")
                    when {
                        response.isSuccessful -> {
                            val users = response.body()?.data?.users ?: emptyList()
                            Timber.d("users $users")
                            val nextKey =
                                if (currentLoadingPageKey < response.body()?.data?.totalPages!!) currentLoadingPageKey + 1 else null

                            return LoadResult.Page(
                                data = users,
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
                                    val refreshToken = authRepository.getRefreshToken()
                                    refreshToken?.let { _refreshToken ->
                                        val newToken =
                                            authRepository.getNewTokens(NewTokensReq(_refreshToken))
                                        newToken?.let { _token ->
                                            val res =
                                                api.searchUser(
                                                    "Bearer $_token",
                                                    currentLoadingPageKey,
                                                    query
                                                )
                                            if (res.isSuccessful) {
                                                val users = res.body()?.data?.users ?: emptyList()

                                                val nextKey =
                                                    if (currentLoadingPageKey < res.body()?.data?.totalPages!!) currentLoadingPageKey + 1 else null

                                                return LoadResult.Page(
                                                    data = users,
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
        return LoadResult.Error(Throwable("Req"))
    }

    override fun getRefreshKey(state: PagingState<Int, SearchUser>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
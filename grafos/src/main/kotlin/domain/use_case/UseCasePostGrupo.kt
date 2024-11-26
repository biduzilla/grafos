package org.example.domain.use_case

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.example.data.repository.RepositoryImpl
import org.example.domain.models.RequestGrupo
import org.example.domain.models.ResponseGrupo
import org.example.utils.Resource
import retrofit2.HttpException
import java.io.IOException

class UseCasePostGrupo {
    private val repository by lazy {
        RepositoryImpl()
    }

    operator fun invoke(requestGrupo: RequestGrupo): Flow<Resource<ResponseGrupo>> = flow {
        try {
            emit(Resource.Loading())
            repository.postGrupo(requestGrupo).let { result ->
                if (result.isSuccessful) {
                    result.body()?.let {
                        emit(Resource.Success(it))
                    }
                } else {
                    emit(Resource.Error(result.errorBody()?.string()?:"Error desconhecido"))
                }
            }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "Error inesperado"))
        } catch (e: IOException) {
            emit(Resource.Error("Cheque sua conexão com a internet"))
        }
    }
}
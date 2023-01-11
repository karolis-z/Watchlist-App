package com.myapplications.mywatchlist.data.local.details

import com.myapplications.mywatchlist.core.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

interface LocalDetailsDataSource {

}

class LocalDetailsDataSourceImpl @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : LocalDetailsDataSource {

}
package com.phonecleaner.aicleaner.data.remote

import java.io.IOException

class ApiHttpException(val code: Int, message: String) : IOException(message)

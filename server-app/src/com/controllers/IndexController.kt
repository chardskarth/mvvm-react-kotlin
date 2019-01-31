package com.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.util.FileCopyUtils
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import java.nio.charset.StandardCharsets

@RestController
class IndexController @Autowired
constructor(private val resourceLoader: ResourceLoader) {

	@RequestMapping(value = ["/{spring:(?!api|main-app-resource)[\\w-]+}/**", ""])
	@Throws(IOException::class)
	fun mainAppIndex(): String {
		val mainAppInputStream = this.resourceLoader.getResource("classpath:public/main-app/index.html")
				.inputStream
		val byteArr = FileCopyUtils.copyToByteArray(mainAppInputStream)
		return String(byteArr, StandardCharsets.UTF_8)
	}

	@RequestMapping(value = ["apple-app-site-association"], method = [RequestMethod.GET], produces = ["application/json"])
	@Throws(IOException::class)
	fun index(): String {
		val mainAppInputStream = this.resourceLoader
				.getResource("classpath:data/apple-app-site-association.json").inputStream
		val byteArr = FileCopyUtils.copyToByteArray(mainAppInputStream)
		return String(byteArr, StandardCharsets.UTF_8)
	}
}

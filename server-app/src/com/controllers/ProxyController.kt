package com.controllers

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.util.UriComponentsBuilder
import java.lang.String.format
import java.net.URI
import java.net.URISyntaxException
import javax.servlet.http.HttpServletRequest
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory


@Controller
class ProxyController {
	@Value("\${server.context-path}")
	private val contextPath: String? = null

	@Value("#{systemProperties['CORP_PROXY_TO_URL'] ?: '\${routes.proxy-to-url}'}")
	private val proxyToUrl: String? = null

	@Value("#{systemProperties['CORP_PROXY_CLIENT_ID'] ?: '\${routes.proxy-to-headers.x-client-id}'}")
	private val proxyToClientId: String? = null

	@Value("#{systemProperties['CORP_PROXY_CLIENT_SECRET'] ?: '\${routes.proxy-to-headers.x-client-secret}'}")
	private val proxyToClientSecret: String? = null

	private fun buildProxyHeaders(request: HttpServletRequest): HttpHeaders {
		val headers = HttpHeaders()
		val headerNames = request.headerNames
		while (headerNames.hasMoreElements()) {
			val headerName = headerNames.nextElement()
			headers.set(headerName, request.getHeader(headerName))
		}
		headers.set("x-client-id", proxyToClientId)
		headers.set("x-client-secret", proxyToClientSecret)
		return headers
	}

	@Throws(URISyntaxException::class)
	private fun buildWebApiUri(request: HttpServletRequest): URI {
		var toReplace = "/api"
		if (contextPath != null && contextPath.trim { it <= ' ' } != "/") {
			toReplace = contextPath + toReplace
		}
		val newRequestURI = request.requestURI.replace(toReplace, "")

		return UriComponentsBuilder
				.fromUri(URI(proxyToUrl))
				.path(newRequestURI)
				.query(request.queryString)
				.build(true)
				.toUri()
	}

	private fun buildMultipartHttpEntity(
			multipartRequest: MultipartHttpServletRequest
	): MultiValueMap<String, HttpEntity<ByteArray>> {
		val retVal = LinkedMultiValueMap<String, HttpEntity<ByteArray>>()
		val fileMap = multipartRequest.fileMap
		for ((key, value) in fileMap) {
			val fileHeaders = HttpHeaders()
			if (StringUtils.hasText(value.contentType)) {
				fileHeaders.add(HttpHeaders.CONTENT_TYPE, value.contentType)
			} else {
				fileHeaders.contentType = MediaType.APPLICATION_OCTET_STREAM
			}
			fileHeaders.add(
				HttpHeaders.CONTENT_DISPOSITION,
				format("form-data; name=\"%s\"; filename=\"%s\"", value.name, value.originalFilename)
			)
			retVal.add(key, HttpEntity(value.bytes, fileHeaders))
		}
		return retVal
	}

	private fun <T> restTemplateExchangeWithPatch(
		uri: URI,
		method: HttpMethod,
		httpEntity: HttpEntity<T>
	) : ResponseEntity<ByteArray> {
		try {
			val requestFactory = HttpComponentsClientHttpRequestFactory()
			return RestTemplate(requestFactory).exchange(
					uri,
					method,
					httpEntity,
					ByteArray::class.java
			)
		} catch (clientErrorException: HttpClientErrorException) {
			return ResponseEntity(
					clientErrorException.responseBodyAsByteArray,
					clientErrorException.responseHeaders,
					clientErrorException.statusCode
			)
		}
	}

	@RequestMapping(value = ["/api/**"], headers = ["content-type=multipart/form-data"])
	@Throws(URISyntaxException::class)
	fun mirrorFileRest(
			multipartRequest: MultipartHttpServletRequest
	): ResponseEntity<ByteArray> {

		val headers = buildProxyHeaders(multipartRequest)
		val uri = buildWebApiUri(multipartRequest)
		val filesHttpEntity = buildMultipartHttpEntity(multipartRequest)
		val httpEntity = HttpEntity(filesHttpEntity, headers)

		println("multipart form Proxy change to: " + uri.toString())
		return restTemplateExchangeWithPatch(uri, HttpMethod.POST, httpEntity)
	}

	@RequestMapping("/api/**")
	@Throws(URISyntaxException::class)
	fun mirrorRest(
			@RequestBody(required = false) body: String?,
			request: HttpServletRequest
	): ResponseEntity<ByteArray> {

		val headers = buildProxyHeaders(request)
		val uri = buildWebApiUri(request)
		val method = HttpMethod.resolve(request.method)!!
		val httpEntity = HttpEntity(body, headers)

		println("Proxy change to: " + uri.toString())
		return restTemplateExchangeWithPatch(uri, method, httpEntity)
	}
}

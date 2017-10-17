/*
 * Copyright 2017 original authors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.particleframework.http.server.binding.binders;

import org.particleframework.core.bind.annotation.AbstractAnnotatedArgumentBinder;
import org.particleframework.core.bind.annotation.AnnotatedArgumentBinder;
import org.particleframework.core.convert.ConversionService;
import org.particleframework.core.convert.ConvertibleMultiValues;
import org.particleframework.core.convert.ConvertibleValues;
import org.particleframework.http.HttpMethod;
import org.particleframework.http.HttpRequest;
import org.particleframework.http.annotation.Parameter;
import org.particleframework.core.type.Argument;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Optional;

/**
 * An {@link AnnotatedArgumentBinder} implementation that uses the {@link Parameter}
 * to trigger binding from an HTTP request parameter
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public class ParameterAnnotationBinder<T> extends AbstractAnnotatedArgumentBinder<Parameter, T, HttpRequest> implements AnnotatedRequestArgumentBinder<Parameter, T> {
    public ParameterAnnotationBinder(ConversionService<?> conversionService) {
        super(conversionService);
    }

    @Override
    public Class<Parameter> getAnnotationType() {
        return Parameter.class;
    }

    @Override
    public Optional<T> bind(Argument<T> argument, HttpRequest source) {
        ConvertibleMultiValues<String> parameters = source.getParameters();
        Parameter annotation = argument.getAnnotation(Parameter.class);
        String parameterName = annotation == null ? argument.getName() : annotation.value();

        Locale locale = source.getLocale();
        Charset characterEncoding = source.getCharacterEncoding();

        Optional<T> result = doBind(argument, parameters, parameterName, locale, characterEncoding);
        if(!result.isPresent() && annotation == null && HttpMethod.requiresRequestBody(source.getMethod())) {
            Optional<ConvertibleValues> body = source.getBody(ConvertibleValues.class);
            if(body.isPresent()) {
                return doBind(argument, body.get(), parameterName, locale, characterEncoding);
            }
        }
        return result;
    }
}
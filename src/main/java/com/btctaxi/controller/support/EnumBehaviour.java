package genesis.accounting.controller.support;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * fh @ 2018-06-08 13:47:37
 */
public interface EnumBehaviour {

    int getValue();

    String getDesc();

    Map<Class<? extends EnumBehaviour>, Map<Integer, ? extends EnumBehaviour>> cachedMap = Maps.newConcurrentMap();

    static <T extends EnumBehaviour> T of(Class<T> t, Integer value) {
        if (Objects.isNull(t) || Objects.isNull(value)) {
            return null;
        }

        Map<Integer, ? extends EnumBehaviour> valueMap = cachedMap.get(t);
        if (Objects.isNull(valueMap)) {
            valueMap = ofCodeKeyMap(t);
            if (valueMap != null) {
                cachedMap.put(t, valueMap);
            }
        }
        if (valueMap == null) {
            throw new RuntimeException("未知类型：Enum " + t.getName() + ";value " + value);
        }
        return (T) valueMap.get(value);

    }

    static <T extends EnumBehaviour> Map<Integer, ? extends EnumBehaviour> ofCodeKeyMap(Class<T> t) {
        ImmutableMap<Integer, ? extends EnumBehaviour> valueMap = null;
        try {
            Method method = t.getMethod("values");
            T[] values = (T[]) method.invoke(null);
            valueMap = Maps.uniqueIndex(Lists.newArrayList(values), input -> input.getValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return valueMap;
    }

}

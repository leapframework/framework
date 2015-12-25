 SELECT  
			t.id `couponId`,
			t.coupon_type,
			t.bg_coupon,
			t.shop_id,
			c.shop_name,
			t.goods_id `goods.goodsId`, 
			a.goods_name `goods.goodsName`,
			b.`id` `goods.pictures.goodsAttachmentId`,			
			CONCAT(:picPrefix,  b.`url`) `goods.pictures.url`,
		 	CONCAT(:picPrefix,  b.`thumbnail_url`) `goods.pictures.thumbnailUrl`,
			a.price `goods.price`,
			t.discount_price,
			t.amount_max,
			t.amount_cut,
			t.end_time,
			t.`status`,
			REPLACE(:qrCodeLink, 'couponId', t.id) `qrCode`
		FROM t_user_coupon tb
		LEFT JOIN t_coupon t ON t.ID = tb.COUPON_ID
		LEFT JOIN t_goods a ON t.goods_id = a.ID
		LEFT JOIN T_GOODS_ATTACHMENT b ON t.goods_id = b.GOODS_ID
		LEFT JOIN t_shop c ON t.SHOP_ID = c.ID
		WHERE 1=1
		AND tb.`status` = 0
		{? AND t.SHOP_ID = :shopId }
		{? AND t.COUPON_TYPE = :type }
		{? AND t.id < :lastId }
		{? AND tb.USER_ID = :userId }
		order by :caac
		{? Limit :count	};
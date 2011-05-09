package org.broadleafcommerce.core.offer.domain;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryImpl;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.domain.ProductImpl;
import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.catalog.domain.SkuImpl;
import org.broadleafcommerce.core.offer.service.OfferDataItemProvider;
import org.broadleafcommerce.core.offer.service.discount.PromotionDiscount;
import org.broadleafcommerce.core.offer.service.discount.PromotionQualifier;
import org.broadleafcommerce.core.offer.service.type.OfferDiscountType;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItemImpl;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.type.OrderItemType;
import org.broadleafcommerce.money.Money;

public class CandidateItemOfferTest extends TestCase {
	
	private CandidateItemOfferImpl candidate;
	private Offer offer;
	private DiscreteOrderItem orderItem1;

	@Override
	protected void setUp() throws Exception {
		OfferDataItemProvider dataProvider = new OfferDataItemProvider();
		
		offer = dataProvider.createItemBasedOfferWithItemCriteria(
			"order.subTotal.getAmount()>20", 
			OfferDiscountType.PERCENT_OFF, 
			"([MVEL.eval(\"toUpperCase()\",\"test1\"), MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))", 
			"([MVEL.eval(\"toUpperCase()\",\"test1\"), MVEL.eval(\"toUpperCase()\",\"test2\")] contains MVEL.eval(\"toUpperCase()\", discreteOrderItem.category.name))"
		).get(0);
		
		candidate = new CandidateItemOfferImpl();
		
		Category category1 = new CategoryImpl();
		category1.setName("test1");
		category1.setId(1L);
		
		Product product1 = new ProductImpl();
		product1.setName("test1");
		
		Sku sku1 = new SkuImpl();
		sku1.setName("test1");
		sku1.setDiscountable(true);
		sku1.setRetailPrice(new Money(19.99D));
		product1.getAllSkus().add(sku1);
		
		category1.getAllProducts().add(product1);
		
		Category category2 = new CategoryImpl();
		category2.setName("test2");
		category2.setId(2L);
		
		Product product2 = new ProductImpl();
		product2.setName("test2");
		
		Sku sku2 = new SkuImpl();
		sku2.setName("test2");
		sku2.setDiscountable(true);
		sku2.setRetailPrice(new Money(29.99D));
		product2.getAllSkus().add(sku2);
		
		category2.getAllProducts().add(product2);
		
		orderItem1 = new DiscreteOrderItemImpl();
		orderItem1.setCategory(category1);
		orderItem1.setName("test1");
		orderItem1.setOrderItemType(OrderItemType.DISCRETE);
		orderItem1.setProduct(product1);
		orderItem1.setQuantity(2);
		orderItem1.setSku(sku1);
		orderItem1.setRetailPrice(new Money(19.99D));
		orderItem1.setPrice(new Money(19.99D));
		
		List<OrderItem> items = new ArrayList<OrderItem>();
		items.add(orderItem1);
		
		candidate.getCandidateTargets().addAll(items);
		candidate.setOffer(offer);
	}
	
	public void testCalculatePotentialSavings() throws Exception {
		Money savings = candidate.calculatePotentialSavings();
		assertTrue(savings.equals(new Money(2D)));
	}
	
	public void testCalculateSavingsForOrderItem() throws Exception {
		Money savings = candidate.calculateSavingsForOrderItem(orderItem1, 1);
		assertTrue(savings.equals(new Money(2D)));
		
		offer.setDiscountType(OfferDiscountType.AMOUNT_OFF);
		savings = candidate.calculateSavingsForOrderItem(orderItem1, 1);
		assertTrue(savings.equals(new Money(10D)));
		
		offer.setDiscountType(OfferDiscountType.FIX_PRICE);
		savings = candidate.calculateSavingsForOrderItem(orderItem1, 1);
		assertTrue(savings.equals(new Money(19.99D - 10D)));
	}
	
	public void testCalculateMaximumNumberOfUses() throws Exception {
		int maxOfferUses = candidate.calculateMaximumNumberOfUses();
		//based on criteria, the max would be 2, but the maxUses on the offer limits it to 1
		assertTrue(maxOfferUses == 1);
		
		offer.setMaxUses(2);
		maxOfferUses = candidate.calculateMaximumNumberOfUses();
		assertTrue(maxOfferUses == 2);
	}
	
	public void testCalculateMaxUsesForItemCriteria() throws Exception {
		int maxItemCriteriaUses = candidate.calculateMaxUsesForItemCriteria(offer.getTargetItemCriteria(), offer);
		assertTrue(maxItemCriteriaUses == 2);
		
		PromotionQualifier qualifier = new PromotionQualifier();
		qualifier.setPromotion(offer);
		qualifier.setQuantity(1);
		orderItem1.getPromotionQualifiers().add(qualifier);
		
		maxItemCriteriaUses = candidate.calculateMaxUsesForItemCriteria(offer.getTargetItemCriteria(), offer);
		assertTrue(maxItemCriteriaUses == 1);
		
		PromotionDiscount discount = new PromotionDiscount();
		discount.setPromotion(offer);
		discount.setQuantity(1);
		orderItem1.getPromotionDiscounts().add(discount);
		
		maxItemCriteriaUses = candidate.calculateMaxUsesForItemCriteria(offer.getTargetItemCriteria(), offer);
		assertTrue(maxItemCriteriaUses == 0);
	}
}
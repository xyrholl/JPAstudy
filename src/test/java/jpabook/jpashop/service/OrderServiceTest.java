package jpabook.jpashop.service;

import org.assertj.core.api.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;

import static org.junit.jupiter.api.Assertions.*;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }

    private Book createBook(String name, int price, int stockQantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQantity);
        em.persist(book);
        return book;
    }

    //통합 테스트 이므로 좋은 TDD예시는 아님
    //유닛테스트로 재작성 요망
    @Test
    void 상품주문() {
        //given
        Member member = createMember();

        Book book = createBook("시골 JPA", 10000, 10);

        //when
        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER, getOrder.getStatus(), "상품 주문시 상태는 ORDER");
        assertEquals(1, getOrder.getOrderItems().size(), "주문한 상품 종류 수가 정확해야한다.");
        assertEquals(10000 * orderCount, getOrder.getTotalPrice() ,"주문 가격은 가격 * 수향이다.");
        assertEquals(8, book.getStockQuantity(),  "주문 수량만큼 재고가 줄어야 한다.");
    }

    // remove stock()에 대한 단위테스트로 구성하는것이 더 좋은 테스트코드
    @Test
    void 상품주문_재고수량초과() {
        //given
        Member member = createMember();
        Item item = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;

        //when
        NotEnoughStockException thrown = assertThrows(NotEnoughStockException.class, () -> 
            orderService.order(member.getId(), item.getId(), orderCount),
            "재고 수량이 부족하면 예외가 발생한다."
        );

        //then
        assertEquals(thrown.getMessage(), "need more stock");
    }

    @Test
    void 주문취소() {
        //given
        Member member = createMember();
        Item item = createBook("시골 JPA", 10000, 10);

        int orderCount =2;
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);
        
        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL, getOrder.getStatus(), "주문 취소시 상태는 CANCEL 이다.");
        assertEquals(10, item.getStockQuantity() ,"주문이 취소된 상품은 그만큼 재고가 증가해야한다.");

    }

}

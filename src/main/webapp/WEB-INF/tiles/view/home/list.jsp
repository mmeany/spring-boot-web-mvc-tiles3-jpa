<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div>
    <table>
        <caption>Visitor List</caption>
        <thead>
            <tr>
                <th>Name</th>
                <th>Number of visits</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="user" items="${page.content}">
            <tr>
                <td>${user.name}</td>
                <td>${user.numberOfVisits}</td>
            </tr>
            </c:forEach>
        </tbody>
    </table>
</div>
<div>Viewing page ${page.number + 1} of ${page.totalPages}</div>
<div>
    <c:forEach var="i" begin="1" end="${page.totalPages}">
    <c:choose>
        <c:when test="${i == page.number + 1}">${i}</c:when>
        <c:otherwise><a href="?page=${i - 1}">${i}</a></c:otherwise>
    </c:choose>
    </c:forEach>
</div>


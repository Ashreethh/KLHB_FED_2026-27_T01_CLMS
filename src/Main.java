        try {
            int count = Integer.parseInt(sc.nextLine());
            if (count < 1) { System.out.println("Copies must be positive."); return; }
            Book old = findBook(title);
            if (old == null) books.add(new Book(title, count));
            else old.setCopies(old.getCopies() + count);
            saveData();
            System.out.println("Book added.");
        } catch (NumberFormatException e) { System.out.println("Enter a whole number."); }
    }

    static void viewBooks() {
        if (books.isEmpty()) { System.out.println("No books yet."); return; }
        books.stream().sorted(Comparator.comparing(Book::getTitle))
             .forEach(System.out::println);
    }

    static void searchBook() {
        System.out.print("Enter title or part of title: ");
        String text = sc.nextLine().trim().toLowerCase();
        List<Book> result = books.stream()
            .filter(b -> b.getTitle().toLowerCase().contains(text))
            .collect(Collectors.toList());
        if (result.isEmpty()) System.out.println("No matching book found.");
        else result.forEach(System.out::println);
    }

    static void registerMember() {
        System.out.print("Member ID: ");
        String id = sc.nextLine().trim();
        if (id.isEmpty() || members.stream().anyMatch(m -> m.getId().equals(id))) {
            System.out.println("ID is empty or already registered."); return;
        }
        System.out.print("Member name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Name cannot be empty."); return; }
        members.add(new Member(id, name));
        saveData();
        System.out.println("Member registered.");
    }

    static void borrowBook() {
        System.out.print("Member ID: ");
        String id = sc.nextLine().trim();
        boolean memberExists = members.stream().anyMatch(m -> m.getId().equals(id));
        if (!memberExists) { System.out.println("Register the member first."); return; }
        long activeLoans = loans.stream().filter(l -> l.getMemberId().equals(id)).count();
        if (activeLoans >= 3) { System.out.println("Borrowing limit is 3 books."); return; }
        System.out.print("Book title: ");
        String title = sc.nextLine().trim();
        Book book = findBook(title);
        if (book == null || book.getCopies() == 0) {
            System.out.println("Book not found or no copies available."); return;
        }
        boolean alreadyBorrowed = loans.stream().anyMatch(l ->
            l.getMemberId().equals(id) && l.getBookTitle().equalsIgnoreCase(title));
        if (alreadyBorrowed) { System.out.println("You already borrowed this title."); return; }
        loans.add(new Loan(id, book.getTitle(), LocalDate.now().plusDays(14)));
        book.setCopies(book.getCopies() - 1);
        saveData();
        System.out.println("Borrowed successfully. Due date: " + LocalDate.now().plusDays(14));
    }

    static void returnBook() {
        System.out.print("Member ID: ");
        String id = sc.nextLine().trim();
        System.out.print("Book title: ");
        String title = sc.nextLine().trim();
        for (int i = 0; i < loans.size(); i++) {
            Loan loan = loans.get(i);
            if (loan.getMemberId().equals(id) && loan.getBookTitle().equalsIgnoreCase(title)) {
                long lateDays = Math.max(0, ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now()));
                System.out.println("Fine: Rs. " + (lateDays * 2));
                Book book = findBook(loan.getBookTitle());
                if (book != null) book.setCopies(book.getCopies() + 1);
                loans.remove(i);
                saveData();
                System.out.println("Book returned.");
                return;
            }
        }
        System.out.println("No matching active loan found.");
    }

    static void overdueReport() {
        List<Loan> overdue = loans.stream()
            .filter(l -> LocalDate.now().isAfter(l.getDueDate()))
            .sorted(Comparator.comparing(Loan::getDueDate))
            .collect(Collectors.toList());
        if (overdue.isEmpty()) { System.out.println("No overdue books."); return; }
        for (Loan l : overdue) {
            long days = ChronoUnit.DAYS.between(l.getDueDate(), LocalDate.now());
            System.out.println(l.getBookTitle() + " | Member: " + l.getMemberId()
                + " | Days late: " + days + " | Fine: Rs. " + (days * 2));
        }
    }

    static void mostBorrowed() {
        Map<String, Long> counts = loans.stream().collect(
            Collectors.groupingBy(Loan::getBookTitle, Collectors.counting()));
        if (counts.isEmpty()) { System.out.println("No borrowing records yet."); return; }
        counts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .forEach(e -> System.out.println(e.getKey() + " - active loans: " + e.getValue()));
    }

    static void saveData() {
        List<String> lines = new ArrayList<>();
        for (Book b : books) lines.add("B|" + clean(b.getTitle()) + "|" + b.getCopies());
        for (Member m : members) lines.add("M|" + clean(m.getId()) + "|" + clean(m.getName()));
        for (Loan l : loans) lines.add("L|" + clean(l.getMemberId()) + "|"
                + clean(l.getBookTitle()) + "|" + l.getDueDate());
        try { Files.write(file, lines); }
        catch (IOException e) { System.out.println("Could not save data: " + e.getMessage()); }
    }

    static String clean(String text) { return text.replace("|", "/").trim(); }

    static void loadData() {
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file)) {
                try {
                    String[] p = line.split("\\|", -1);
                    if (p[0].equals("B") && p.length == 3) books.add(new Book(p[1], Integer.parseInt(p[2])));
                    else if (p[0].equals("M") && p.length == 3) members.add(new Member(p[1], p[2]));
                    else if (p[0].equals("L") && p.length == 4) loans.add(new Loan(p[1], p[2], LocalDate.parse(p[3])));
                } catch (RuntimeException e) { System.out.println("Skipped invalid data row."); }
